package ktb.billage.api.group;

import ktb.billage.apidoc.GroupApiDoc;
import ktb.billage.application.group.GroupFacade;
import ktb.billage.domain.group.dto.GroupRequest;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class GroupController implements GroupApiDoc {
    private final GroupFacade groupFacade;

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody GroupRequest.Create request,
                                         @AuthenticatedId Long userId) {
        Long newGroupId = groupFacade.createGroup(userId, request.groupName(), request.groupCoverImageUrl());

        return ResponseEntity.status(CREATED)
                .body(Map.of("groupId", newGroupId));
    }
}
