package com.lzq.networkstatelistener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipCompressor {
     public ZipCompressor() {}

     /**@param srcFiles 需压缩的文件路径及文件名
     * @param desFile 保存的文件名及路径
     * <a href="http://my.oschina.net/u/556800" class="referer" target="_blank">@return</a>  如果压缩成功返回true
     */
     public boolean zipCompress(String[] srcFiles, String desFile) {
          boolean isSuccessful = false;

          String[] fileNames = new String[srcFiles.length-1];
          for (int i = 0; i < srcFiles.length-1; i++) {
               fileNames[i] = parse(srcFiles[i]);
          }

          try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desFile));
                    ZipOutputStream zos = new ZipOutputStream(bos);
                    String entryName = null;
                    entryName = fileNames[0];

                    for (int i = 0; i < fileNames.length; i++) {
                         entryName = fileNames[i];

                         // 创建Zip条目
                         ZipEntry entry = new ZipEntry(entryName);
                         zos.putNextEntry(entry);

                         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFiles[i]));

                         byte[] b = new byte[1024];

                         while (bis.read(b, 0, 1024) != -1) {
                              zos.write(b, 0, 1024);
                         }
                         bis.close();
                         zos.closeEntry();
                    }

                    zos.flush();
                    zos.close();
                    isSuccessful = true;
          } catch (IOException e) {
          }

          return isSuccessful;
     }


     // 解析文件名
     private String parse(String srcFile) {
          int location = srcFile.lastIndexOf("/");
          String fileName = srcFile.substring(location + 1);
          return fileName;
     }
     
     
}