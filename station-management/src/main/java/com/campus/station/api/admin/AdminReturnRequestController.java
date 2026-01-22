package com.campus.station.api.admin;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.AdminRoleScope;
import com.campus.station.model.Parcel;
import com.campus.station.model.ReturnRequest;
import com.campus.station.model.SysAdmin;
import com.campus.station.service.AdminRoleScopeService;
import com.campus.station.service.ParcelService;
import com.campus.station.service.ReturnRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "AdminReturnRequest", description = "管理员退货申请管理接口")
@RequestMapping("/api/admin/return-request")
public class AdminReturnRequestController {

    private final ReturnRequestService service;
    private final ParcelService parcelService;
    private final AdminRoleScopeService adminRoleScopeService;

    public AdminReturnRequestController(ReturnRequestService service,
                                        ParcelService parcelService,
                                        AdminRoleScopeService adminRoleScopeService) {
        this.service = service;
        this.parcelService = parcelService;
        this.adminRoleScopeService = adminRoleScopeService;
    }

    private SysAdmin requireAdmin() {
        SysAdmin current = SessionUtil.getCurrentAdmin();
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "管理员未登录");
        }
        return current;
    }

    private AdminRoleScope requireCurrentAdminScope() {
        SysAdmin admin = requireAdmin();
        return adminRoleScopeService.getByAdminId(admin.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员角色未配置"));
    }

    @GetMapping
    @Operation(summary = "分页查询退货申请")
    public Page<ReturnRequest> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Integer status) {
        AdminRoleScope scope = requireCurrentAdminScope();
        Pageable pageable = PageRequest.of(page, size);
        
        // Fetch all (or by status) first, then filter by scope
        // Note: Ideally this should be done in DB, but due to complex scope logic and cross-table check,
        // we filter in memory for now. For large datasets, this needs optimization.
        
        List<ReturnRequest> allRequests;
        if (status != null) {
            allRequests = service.listByStatus(status, Pageable.unpaged()).getContent();
        } else {
            allRequests = service.list(Pageable.unpaged()).getContent();
        }
        
        List<ReturnRequest> filtered = allRequests.stream()
                .filter(req -> {
                    return parcelService.getByTrackingNumber(req.getTrackingNumber())
                            .map(parcel -> parcelService.isParcelVisibleForScope(scope, parcel))
                            .orElse(false); // Hide if parcel not found
                })
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        
        if (start > filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "审核退货申请 (同意/拒绝)")
    public ResponseEntity<ReturnRequest> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        requireAdmin();
        if (status != 1 && status != 2) {
            return ResponseEntity.badRequest().build(); // Only allow Approve(1) or Reject(2)
        }
        
        ReturnRequest request = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "退货申请不存在"));
                
        // Optional: Verify admin has scope to modify this request
        AdminRoleScope scope = requireCurrentAdminScope();
        boolean visible = parcelService.getByTrackingNumber(request.getTrackingNumber())
                .map(parcel -> parcelService.isParcelVisibleForScope(scope, parcel))
                .orElse(false);
        if (!visible) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此退货申请");
        }
        
        ReturnRequest updated = service.updateStatus(id, status);
        
        // If approved (1), update Parcel status
        if (status == 1) {
            parcelService.getByTrackingNumber(updated.getTrackingNumber())
                .ifPresent(parcel -> {
                    Parcel updateDto = new Parcel();
                    updateDto.setIsReturned(1);
                    parcelService.update(parcel.getId(), updateDto);
                });
        }
        
        return ResponseEntity.ok(updated);
    }
}
