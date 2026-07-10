package com.resumefit.resumefit.contoller;


import com.resumefit.resumefit.entity.Resume;
import com.resumefit.resumefit.entity.User;
import com.resumefit.resumefit.repository.UserRepository;
import com.resumefit.resumefit.security.JwtUtil;
import com.resumefit.resumefit.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resume")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is missing or empty");
        }

        try {
            Long effectiveUserId = userId;

            if (authorization != null && authorization.startsWith("Bearer ")) {
                String raw = authorization.replace("Bearer ", "").trim();
                String email = jwtUtil.extractEmail(raw);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    effectiveUserId = user.getId();
                }
            }

            if (effectiveUserId == null) {
                return ResponseEntity.badRequest().body("User id missing and could not be inferred from token");
            }

            Resume resume = resumeService.uploadResume(effectiveUserId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(resume);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
