package com.resumefit.resumefit.service;



import com.resumefit.resumefit.entity.User;
import com.resumefit.resumefit.entity.Resume;
import com.resumefit.resumefit.repository.UserRepository;
import com.resumefit.resumefit.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
// java.io.File not needed; using java.nio.file.Path
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;

    public Resume uploadResume(Long userId, MultipartFile file) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for id: " + userId));

        // Use a stable absolute directory under the user's home to avoid Tomcat temp paths
        Path uploadDir = Paths.get(System.getProperty("user.home"), "resumefit-uploads");
        Files.createDirectories(uploadDir);

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        Path savedPath = uploadDir.resolve(System.currentTimeMillis() + "_" + original);
        file.transferTo(savedPath.toFile());

        if (!Files.exists(savedPath) || !Files.isReadable(savedPath)) {
            throw new IllegalStateException("Saved file not found or not readable: " + savedPath.toString());
        }

        String extractedText = pdfService.extractText(savedPath.toFile());

        Resume resume = new Resume(
            user,
            file.getOriginalFilename(),
            extractedText
        );

        return resumeRepository.save(resume);
    }
}
