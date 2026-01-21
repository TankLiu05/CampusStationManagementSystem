package com.campus.station.service;

import com.campus.station.model.Parcel;
import com.campus.station.model.StationStorage;
import java.util.Optional;

public interface StationStorageService {
    
    /**
     * 根据包裹信息创建或更新库存记录
     * 解析包裹的位置信息 (例如 "A区-1号货架-1234") 并填充到 StationStorage
     */
    StationStorage syncFromParcel(Parcel parcel);

    /**
     * 更新签收状态
     */
    void updateSignStatus(String trackingNumber, Integer isSigned);
    
    Optional<StationStorage> getByTrackingNumber(String trackingNumber);

    /**
     * 多条件查询仓库库存
     * @param area 区域 (A-D)
     * @param shelf 货架号 (1-10)
     * @param position 具体位置码
     * @param receiverName 收件人姓名
     * @param receiverPhone 收件人电话
     * @return 符合条件的库存列表
     */
    /**
     * 根据运单号删除库存
     */
    void deleteByTrackingNumber(String trackingNumber);

    java.util.List<StationStorage> search(String area, String shelf, String position, String receiverName, String receiverPhone);
}
