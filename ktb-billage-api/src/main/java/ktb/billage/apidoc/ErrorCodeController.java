package ktb.billage.apidoc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/docs")
public class ErrorCodeController implements ErrorCodeApiDoc {

    @GetMapping("/errors")
    public ResponseEntity<?> listErrorCodes() {
        return ResponseEntity.ok().build();
    }
}
