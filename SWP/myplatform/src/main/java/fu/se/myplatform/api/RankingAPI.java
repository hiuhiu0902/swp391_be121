package fu.se.myplatform.api;

import fu.se.myplatform.dto.UserRankingDTO;
import fu.se.myplatform.service.RankingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/ranking")
public class RankingAPI {

    @Autowired
    private RankingService rankingService;

//    @GetMapping
//    public ResponseEntity<List<UserRankingDTO>> getRankings() {
//        return ResponseEntity.ok(rankingService.getAllRankings());
//    }
//
//    @GetMapping("/top/{limit}")
//    public ResponseEntity<List<UserRankingDTO>> getTopRankings(@PathVariable int limit) {
//        return ResponseEntity.ok(rankingService.getTopRankings(limit));
//    }
//
//    @GetMapping("/money-saved")
//    public ResponseEntity<List<UserRankingDTO>> getMoneySavedRankings() {
//        return ResponseEntity.ok(rankingService.getMoneySavedRankings());
//    }
//
//    @GetMapping("/money-saved/top/{limit}")
//    public ResponseEntity<List<UserRankingDTO>> getTopMoneySavedRankings(@PathVariable int limit) {
//        return ResponseEntity.ok(rankingService.getTopMoneySavedRankings(limit));
//    }

    @GetMapping("/participation")
    public ResponseEntity<List<UserRankingDTO>> getParticipationRankings() {
        return ResponseEntity.ok(rankingService.getParticipationRankings());
    }

    @GetMapping("/participation/top/{limit}")
    public ResponseEntity<List<UserRankingDTO>> getTopParticipationRankings(@PathVariable int limit) {
        return ResponseEntity.ok(rankingService.getTopParticipationRankings(limit));
    }
}
