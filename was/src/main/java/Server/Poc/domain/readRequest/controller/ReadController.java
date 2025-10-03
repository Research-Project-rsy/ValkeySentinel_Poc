package Server.Poc.domain.readRequest.controller;

import Server.Poc.domain.readRequest.service.ReadService;
import Server.Poc.global.response.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/read")
@RequiredArgsConstructor
public class ReadController {

    private final ReadService readService;

    // 세션 서버에 테스트 데이터 조회 요청
    @GetMapping("/test")
    public ResponseEntity<ApiResponseWrapper<String>> testRead() {

        // 서비스 레이어 호출
        String writtenData = readService.testRead();

        // 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(writtenData, "세션 서버 > 테스트 데이터 정상 조회 완료"));
    }
}
