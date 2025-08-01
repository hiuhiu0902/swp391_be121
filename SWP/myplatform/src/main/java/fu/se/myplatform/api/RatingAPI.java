package fu.se.myplatform.api;

import fu.se.myplatform.dto.RatingRequest;
import fu.se.myplatform.dto.RatingResponse;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.entity.Rating;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.MemberRepository;
import fu.se.myplatform.service.RatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rating")
@SecurityRequirement(
        name = "api"
)
public class RatingAPI {

    @Autowired
    private RatingService ratingService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ModelMapper modelMapper;


    @PostMapping("")
    public ResponseEntity<RatingResponse> addRating(@RequestBody RatingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUserName(username);
        Member member = memberRepository.findByUser(account);
        if (member == null) return ResponseEntity.badRequest().build();
        Coach coach = member.getCoach();
        if (coach == null) return ResponseEntity.badRequest().body(null);

        Rating rating = ratingService.addRating(member, coach, request.getStars(), request.getComment());
        RatingResponse response = modelMapper.map(rating, RatingResponse.class);
        // set các trường cho FE nếu muốn
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RatingResponse>> getAllRatings() {
        List<Rating> ratings = ratingService.getAllRatings();

        // Chuyển đổi từ List<Rating> sang List<RatingResponse> để trả về dữ liệu cần thiết
        List<RatingResponse> responseList = ratings.stream()
                .map(rating -> {
                    RatingResponse response = modelMapper.map(rating, RatingResponse.class);
                    // Lấy thông tin chi tiết để hiển thị cho admin
                    if (rating.getMember() != null && rating.getMember().getUser() != null) {
                        response.setMemberName(rating.getMember().getUser().getFullName());
                        response.setMemberId(rating.getMember().getMemberId());
                    }
                    if (rating.getCoach() != null && rating.getCoach().getUser() != null) {
                        response.setCoachName(rating.getCoach().getUser().getFullName());
                        response.setCoachId(rating.getCoach().getCoachId());
                    }
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
