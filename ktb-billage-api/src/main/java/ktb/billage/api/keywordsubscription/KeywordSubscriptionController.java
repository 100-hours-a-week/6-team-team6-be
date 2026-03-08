package ktb.billage.api.keywordsubscription;

import jakarta.validation.Valid;
import ktb.billage.apidoc.KeywordSubscriptionApiDoc;
import ktb.billage.application.keywordsubscription.KeywordSubscriptionFacade;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionRequest;
import ktb.billage.domain.keywordsubscription.dto.KeywordSubscriptionResponse;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class KeywordSubscriptionController implements KeywordSubscriptionApiDoc {
    private final KeywordSubscriptionFacade keywordSubscriptionFacade;

    @PostMapping("/groups/{groupId}/memberships/me/keyword-subscriptions")
    public ResponseEntity<KeywordSubscriptionResponse.Id> createKeywordSubscription(
            @PathVariable Long groupId,
            @Valid @RequestBody KeywordSubscriptionRequest.Create request,
            @AuthenticatedId Long userId
    ) {
        return ResponseEntity.status(CREATED)
                .body(keywordSubscriptionFacade.registerKeyword(userId, groupId, request.keyword()));
    }

    @DeleteMapping("/groups/{groupId}/memberships/me/keyword-subscriptions/{keywordSubscriptionId}")
    public ResponseEntity<Void> deleteKeywordSubscription(
            @PathVariable Long groupId,
            @PathVariable Long keywordSubscriptionId,
            @AuthenticatedId Long userId
    ) {
        keywordSubscriptionFacade.delete(userId, groupId, keywordSubscriptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups/{groupId}/memberships/me/keyword-subscriptions")
    public ResponseEntity<KeywordSubscriptionResponse.Summaries> getMyKeywordSubscriptions(
            @PathVariable Long groupId,
            @AuthenticatedId Long userId
    ) {
        return ResponseEntity.ok(keywordSubscriptionFacade.getMyKeywordSubscriptionsInGroup(userId, groupId));
    }

}
