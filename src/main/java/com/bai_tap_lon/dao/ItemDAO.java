package com.bai_tap_lon.dao;
import com.bai_tap_lon.model.Item;
import com.bai_tap_lon.factory.ItemFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class ItemDAO { //Data Acess Object, như là 1 ng thủ kho quản lí kho Item mỗi khi cất vào, bỏ ra. File này sinh ra với mục đích để lưu trữ dữ liệu bền vững, kể cả khi đã tắt chương trình thì dũ liệu vẫn lưu vào file rồi.
   private static final String FILE_PATH ="src/main/resources/item.csv";
   public static void saveItems(List<Item> items){ //saveItems dùng để lưu items mới.
       try (BufferedWriter bw= new BufferedWriter(new FileWriter(FILE_PATH))){
           for (Item item : items){
               bw.write(item.toCSV());
               bw.newLine();
           }
       }catch (IOException e){
           System.out.println("Lỗi khi lưu file: " + e.getMessage());
       }
   }
   public static List<Item> loadItems(){ //loadItems dùng để nhả ra các items đã lưu.
       List<Item> items=new ArrayList<>();
       File file =new File(FILE_PATH);
       if (!file.exists()){
           return items;
       }
       try(BufferedReader br = new BufferedReader(new FileReader(file))){
           String line;
           while ((line=br.readLine()) !=null){
               if (line.trim().isEmpty()) continue;
               String[] parts =line.split(",");
               String type=parts[0];
               String id=parts[1];
               String name=parts[2];
               String description =parts[3];
               double initPrice=Double.parseDouble(parts[4]);
               String[] metadata =Arrays.copyOfRange(parts,5,parts.length);
               Item item=ItemFactory.createItem(type,id,name, description, initPrice, metadata);
               if (item != null){
                   items.add(item);
               }
           }
       }catch (IOException|NumberFormatException e) {
           System.err.println("Lỗi khi đọc file hoặc sai định dạng: " + e.getMessage());
       }
       return items;
   }
}
