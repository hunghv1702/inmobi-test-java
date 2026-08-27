package com.hunghv.inmobitestjava.mapper;

import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.generated.model.AuthResponse;
import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;
import com.hunghv.inmobitestjava.generated.model.GuessResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardResponse;
import com.hunghv.inmobitestjava.generated.model.RegisterResponse;
import com.hunghv.inmobitestjava.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    RegisterResponse toRegisterResponse(UserAccount user);

    CurrentUserResponse toCurrentUserResponse(UserAccount user);

    @Mapping(target = "guess", source = "guess")
    @Mapping(target = "serverNumber", source = "serverNumber")
    @Mapping(target = "correct", source = "correct")
    @Mapping(target = "score", source = "user.score")
    @Mapping(target = "turns", source = "user.turns")
    GuessResponse toGuessResponse(UserAccount user, int guess, int serverNumber, boolean correct);

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    AuthResponse toAuthResponse(String accessToken, String refreshToken);

    @Mapping(target = "rank", source = "rank")
    @Mapping(target = "email", source = "view.email")
    @Mapping(target = "score", source = "view.score")
    LeaderboardResponse toLeaderboardResponse(int rank, UserRepository.LeaderboardView view);

    default List<LeaderboardResponse> toLeaderboardResponses(List<UserRepository.LeaderboardView> views) {
        List<LeaderboardResponse> responses = new ArrayList<>(views.size());
        for (int i = 0; i < views.size(); i++) {
            responses.add(toLeaderboardResponse(i + 1, views.get(i)));
        }
        return responses;
    }
}
