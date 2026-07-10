package com.resumefit.resumefit.contoller;



import com.resumefit.resumefit.dto.ATSAnalyzeRequestDTO;
import com.resumefit.resumefit.dto.ATSResponseDTO;
import com.resumefit.resumefit.dto.ResumeHistoryDTO;
import com.resumefit.resumefit.service.ResumeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<ATSResponseDTO> analyze(
            @RequestBody ATSAnalyzeRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        ATSResponseDTO result = resumeAnalysisService.analyzeAndStore(request, authorization);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResumeHistoryDTO>> history(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        List<ResumeHistoryDTO> list = resumeAnalysisService.getUserHistory(authorization);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/history/paged")
    public ResponseEntity<org.springframework.data.domain.Page<ResumeHistoryDTO>> historyPaged(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        var p = resumeAnalysisService.getUserHistoryPaged(authorization, page, size);
        return ResponseEntity.ok(p);
    }
}
