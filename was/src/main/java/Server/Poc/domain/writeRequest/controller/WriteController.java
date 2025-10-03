package Server.Poc.domain.writeRequest.controller;

import Server.Poc.domain.writeRequest.service.WriteService;
import Server.Poc.global.response.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/write")
@RequiredArgsConstructor
public class WriteController {

    private final WriteService writeService;

    // 세션 서버에 테스트 데이터 전송
    @PostMapping("/test")
    public ResponseEntity<ApiResponseWrapper<String>> testWrite() {

        // 서비스 레이어 호출
        String writtenData = writeService.testWrite();

        // 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(writtenData, "세션 서버 > 테스트 데이터 정상 삽입 완료"));
    }



}
