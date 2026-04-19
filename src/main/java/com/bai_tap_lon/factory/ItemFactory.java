package com.bai_tap_lon.factory;
//Tạo ra ItemFactory để tạo nên 1 đối tượng Item mới dễ dàng.
import com.bai_tap_lon.model.*;
public class ItemFactory {
    /**
     * @param type: Loại hàng hóa (Electronics, Art, Vehicle,...)
     * @param id: Mã định danh
     * @param name: Tên sản phẩm
     * @param description: Mô tả sản phẩm
     * @param initPrice: Giá khởi điểm
     * @param metadata: Các thuộc tính riêng biệt   //metadata là 1 cái bình chứa để chứa những dữ liệu sau mà có thể có 1 phần tử, hoặc 2, 3 phần tử tùy trường hợp.
     */
    public static Item createItem(String type, String id, String name,String description,  double initPrice, String...metadata){
        if (type ==null){
            return null;
        }
        switch (type.toUpperCase()){
            case "ELECTRONICS":
                return new Electronics(id, name,description, initPrice, metadata[0],metadata[1],metadata[2],metadata[3]);
            case "ART":
                return new Art(id, name, description, initPrice, metadata[0], Integer.parseInt(metadata[1]), metadata[2]);
            case "VEHICLE":
                return new Vehicle(id, name, description, initPrice, metadata[0], metadata[1], Integer.parseInt(metadata[2]));
            case"FASHION":
                return new Fashion(id, name, description, initPrice, metadata[0], metadata[1], metadata[2]);
            case"FURNITURE":
                return new Furniture(id, name, description, initPrice, metadata[0], metadata[1]);
            default:
                throw new IllegalArgumentException("Loại hàng hóa không xác định." + type);
        }
    }
}
