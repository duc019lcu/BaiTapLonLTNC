package com.bai_tap_lon.dao;

import com.bai_tap_lon.factory.ItemFactory;
import com.bai_tap_lon.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemDAOTest {

    private List<Item> backupItems;

    // Chạy TRƯỚC mỗi bài test: Đọc file item.csv hiện tại và lưu tạm vào RAM
    @BeforeEach
    public void backupData() {
        backupItems = ItemDAO.loadItems();
    }

    // Chạy SAU mỗi bài test: Ghi đè lại dữ liệu cũ từ RAM xuống file item.csv
    @AfterEach
    public void restoreData() {
        ItemDAO.saveItems(backupItems);
    }

    @Test
    public void testSaveAndLoadMultipleItemTypes() {
        // 1. Tạo danh sách ảo chứa 2 loại mặt hàng
        List<Item> mockList = new ArrayList<>();

        // Truyền 4 tham số phụ cho Electronics
        Item electronics = ItemFactory.createItem(
                "Electronics", "E999", "Test Laptop", "Mô tả", 1000.0,
                "Dell", "XPS", "SN123", "2026-01-01"
        );

        // Truyền 3 tham số phụ cho Art (theo code ItemFactory của bạn)
        Item art = ItemFactory.createItem(
                "Art", "A999", "Test Tranh", "Mô tả", 500.0,
                "Picasso", "1500", "Sơn dầu"
        );

        mockList.add(electronics);
        mockList.add(art);

        // 2. Ghi đè file item.csv bằng danh sách ảo
        ItemDAO.saveItems(mockList);

        // 3. Đọc lên và kiểm tra
        List<Item> loadedList = ItemDAO.loadItems();
        assertEquals(2, loadedList.size(), "Số lượng item đọc lên phải là 2");

        // Kiểm tra đa hình: Dữ liệu đọc lên có khôi phục đúng class con không
        Item loadedE = loadedList.get(0);
        assertTrue(loadedE instanceof Electronics, "Item 1 phải được parse thành Electronics");
        assertEquals("E999", loadedE.getId(), "ID Electronics không khớp");

        Item loadedA = loadedList.get(1);
        assertTrue(loadedA instanceof Art, "Item 2 phải được parse thành Art");
        assertEquals("A999", loadedA.getId(), "ID Art không khớp");
    }

    @Test
    public void testSaveAndLoadEmptyList() {
        // 1. Xóa trắng danh sách và lưu xuống file
        ItemDAO.saveItems(new ArrayList<>());

        // 2. Đọc lên và kiểm tra xem có bắt lỗi an toàn không
        List<Item> loadedList = ItemDAO.loadItems();
        assertNotNull(loadedList, "Danh sách không được null khi file rỗng");
        assertTrue(loadedList.isEmpty(), "Danh sách phải rỗng hoàn toàn");
    }
}