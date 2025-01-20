package com.ntt.ntt.Util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//파일 업로드에 관련된 메소드를 담은 클래스
// poster.jpg ==> poster.jpg 저장, poster.jpg ==> poster.jpg 새로 저장기존 내용 되고  새로운 파일 저장
// poster.jpg ==> 213411sdgw2sd23.jpg 중복되지 않는 이름으로 파일 저장
// UUID : 파일이름을 난수로 생성
@Component
public class FileUpload {

    /*---------------------------------------------------------------------
    함수명 : String FileUpload(String imgLocation, List<MultipartFile> imageFile)
    인수 : 저장될 위치, 이미지 파일 리스트
    출력 : 저장 후 생성된 새로운 파일명 리스트
    설명 : 이미지 파일을 새로운 이름으로 지정된 폴더에 저장하고 새로운 이름을 전달
    ---------------------------------------------------------------------*/
    public List<String> FileUpload(String imgLocation, List<MultipartFile> imageFile) {
        List<String> filenames = new ArrayList<>();

        // 이미지 파일 리스트를 반복하면서 각 파일을 처리
        for (MultipartFile file : imageFile) {
            // 이미지 파일의 원래 파일명
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                continue; // 파일명이 없으면 넘어감
            }

            // 확장자 분리
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            UUID uuid = UUID.randomUUID(); // 난수 생성
            String filename = uuid.toString() + extension; // 새로운 파일명 생성
            String path = imgLocation + filename; // 최종 저장 경로

            // 파일 저장
            try {
                File folder = new File(imgLocation);
                if (!folder.exists()) {
                    folder.mkdir(); // 폴더가 없다면 생성
                }

                // 파일을 바이트 배열로 읽어서 저장
                byte[] fileData = file.getBytes();
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(fileData);
                fos.close(); // 파일 저장 완료

                filenames.add(filename); // 파일명 리스트에 추가
            } catch (Exception e) {
                // 예외 발생 시 null 대신 빈 문자열을 반환
                filenames.add(null);
            }
        }

        return filenames; // 파일명 리스트 반환
    }

    /*---------------------------------------------------------------------
    함수명 : void FileDelete(String imgLocation, String imageFileName)
    인수 : 저장될 위치, 이미지 파일명
    출력 : 없음
    설명 : 지정된 파일을 삭제
    ---------------------------------------------------------------------*/
    public void FileDelete(String imgLocation, String imageFileName) {
        // 파일 경로 생성
        String deleteFileName = imgLocation + imageFileName;

        try {
            File deleteFile = new File(deleteFileName);
            if (deleteFile.exists()) {
                deleteFile.delete(); // 파일 삭제
            }
        } catch (Exception e) {
            // 예외가 발생해도 처리하지 않음
            return;
        }
    }
}
