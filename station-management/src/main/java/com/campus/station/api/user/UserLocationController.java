package com.campus.station.api.user;

import com.campus.station.common.SessionUtil;
import com.campus.station.model.Location;
import com.campus.station.model.SysUser;
import com.campus.station.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

        Location created = locationService.create(location);
        return ResponseEntity.ok(created);
    }
}

