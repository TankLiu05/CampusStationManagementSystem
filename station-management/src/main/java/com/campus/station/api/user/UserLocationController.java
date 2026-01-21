package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Location;
import com.campus.station.model.SysUser;
import com.campus.station.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UserLocation", description = "用户收货地址相关接口")
@RequestMapping("/api/user/location")
public class UserLocationController {

    private final LocationService locationService;

    public UserLocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @Operation(summary = "创建收货地址")
    public ResponseEntity<?> create(@RequestBody Location req) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        String username = req.getUsername();
        String phone = req.getPhone();
        String detailAddress = req.getDetailAddress();
        if (username == null || username.isBlank()) {
            username = currentUser.getUsername();
        }
        if (phone == null || phone.isBlank()) {
            phone = currentUser.getPhone();
        }
        if (phone == null || phone.isBlank() || detailAddress == null || detailAddress.isBlank()) {
            return ResponseEntity.badRequest().body("手机号和详细地址为必填项");
        }

        Location location = new Location();
        location.setUserId(currentUser.getId());
        location.setUsername(username);
        location.setPhone(phone);
        location.setProvince(req.getProvince());
        location.setCity(req.getCity());
        location.setStreet(req.getStreet());
        location.setDetailAddress(detailAddress);
        location.setIsDefault(req.getIsDefault());

        Location created = locationService.create(location);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "获取当前用户收货地址列表")
    public ResponseEntity<?> listMyLocations() {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        java.util.List<Location> locations = locationService.listByUserId(currentUser.getId());
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/default")
    @Operation(summary = "获取当前用户默认收货地址")
    public ResponseEntity<?> getDefaultLocation() {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return locationService.getDefaultByUserId(currentUser.getId())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("未找到默认收货地址"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取收货地址详情")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        return locationService.getById(id)
                .map(location -> {
                    if (!location.getUserId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("无权访问该地址");
                    }
                    return ResponseEntity.ok(location);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改收货地址")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Location req) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        return locationService.getById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("无权修改该地址");
                    }
                    
                    Location update = new Location();
                    update.setUsername(req.getUsername());
                    update.setPhone(req.getPhone());
                    update.setProvince(req.getProvince());
                    update.setCity(req.getCity());
                    update.setStreet(req.getStreet());
                    update.setDetailAddress(req.getDetailAddress());
                    update.setIsDefault(req.getIsDefault());
                    
                    try {
                        Location updated = locationService.update(id, update);
                        return ResponseEntity.ok(updated);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.notFound().build();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除收货地址")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        return locationService.getById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("无权删除该地址");
                    }
                    locationService.delete(id);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/default")
    @Operation(summary = "设为默认地址")
    public ResponseEntity<?> setDefault(@PathVariable Long id) {
        SysUser currentUser = SessionUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        return locationService.getById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("无权修改该地址");
                    }
                    locationService.setDefault(currentUser.getId(), id);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
