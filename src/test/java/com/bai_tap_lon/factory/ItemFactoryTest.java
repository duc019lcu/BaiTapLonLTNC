package com.bai_tap_lon.factory;

//ta tạo file này vs mục đích để tạo testcase cho file ItemFactory xem khi add vô thì có đúng ko.
import com.bai_tap_lon.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemFactoryTest {

    @Test
    public void testCreateElectronics() {
        // 1. Chuẩn bị dữ liệu đầu vào (Arrange)
        String type = "Electronics";
        String id = "E001";
        String name = "Laptop Dell";
        String desc = "Laptop gaming cấu hình cao";
        double price = 15000.0;

        // 2. Thực thi hàm cần test (Act)
        // Truyền thẳng 4 cái extraInfo tách rời nhau ra phía sau cùng,
        // Java sẽ tự gom 4 cái này vào mảng metadata[] cho bạn!
        Item item = ItemFactory.createItem(
                type, id, name, desc, price,
                "Dell", "XPS 15", "SN-987654321", "2027-05-04"
        );

        // 3. Kiểm tra kết quả thực tế (Assert)
        assertNotNull(item, "Item không được trả về null");
        assertTrue(item instanceof Electronics, "Factory phải tạo ra đúng đối tượng Electronics");

        // Ép kiểu về Electronics để test sâu hơn các thuộc tính riêng biệt
        Electronics electronics = (Electronics) item;

        // Test các thuộc tính kế thừa từ Item
        assertEquals("E001", electronics.getId(), "ID không khớp");
        assertEquals("Laptop Dell", electronics.getName(), "Tên sản phẩm không khớp");
        assertEquals(15000.0, electronics.getInitPrice(), "Giá khởi điểm không khớp");

        // Test các thuộc tính riêng của Electronics (Giả định getter của bạn tên như thế này)
        assertEquals("Dell", electronics.getBrand(), "Brand không khớp");
        assertEquals("XPS 15", electronics.getModel(), "Model không khớp");
        assertEquals("SN-987654321", electronics.getSerialNumber(), "Serial Number không khớp");
        assertEquals("2027-05-04", electronics.getWarrantyDate(), "Ngày bảo hành không khớp");
    }

    @Test
    public void testCreateArt() {
        // 1. Chuẩn bị dữ liệu (Arrange)
        String type = "Art";
        String id = "A001";
        String name = "Mona Lisa (Copy)";
        String desc = "Bức tranh chân dung nổi tiếng";
        double price = 5000000.0;

        // 2. Thực thi (Act)
        // Truyền 3 tham số phụ cho Art: artist, yearCreated (truyền dạng chuỗi), technique
        Item item = ItemFactory.createItem(
                type, id, name, desc, price,
                "Leonardo da Vinci", "1503", "Sơn dầu"
        );

        // 3. Kiểm tra kết quả (Assert)
        assertNotNull(item, "Item không được trả về null");
        assertTrue(item instanceof Art, "Factory phải tạo ra đúng đối tượng Art");

        // Ép kiểu về Art để test các thuộc tính riêng biệt
        Art art = (Art) item;

        // Test thuộc tính chung
        assertEquals("A001", art.getId(), "ID không khớp");
        assertEquals("Mona Lisa (Copy)", art.getName(), "Tên sản phẩm không khớp");
        assertEquals(5000000.0, art.getInitPrice(), "Giá khởi điểm không khớp");

        // Test thuộc tính riêng của Art (Giả định bạn đã tạo getter tương ứng)
        assertEquals("Leonardo da Vinci", art.getArtist(), "Tên họa sĩ không khớp");
        // Lưu ý: yearCreated là int, nên kỳ vọng ở đây là số nguyên 1503
        assertEquals(1503, art.getYearCreated(), "Năm sáng tác không khớp");
        assertEquals("Sơn dầu", art.getTechnique(), "Kỹ thuật không khớp");
    }
}
