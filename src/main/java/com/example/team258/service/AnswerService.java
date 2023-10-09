package com.example.team258.service;

import com.example.team258.dto.AnswerRequestDto;
import com.example.team258.dto.AnswerResponseDto;
import com.example.team258.entity.Answer;
import com.example.team258.dto.MessageDto;
import com.example.team258.entity.Survey;
import com.example.team258.entity.User;
import com.example.team258.repository.AnswerRepository;
import com.example.team258.repository.SurveyRepository;
import com.example.team258.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    @Transactional//(isolation = Isolation.REPEATABLE_READ)
    public MessageDto createAnswer(AnswerRequestDto requestDto, User user) {
        user = userRepository.findById(user.getUserId()).orElseThrow(()->new NullPointerException("예외가 발생하였습니다."));
        Survey survey = surveyRepository.findById(requestDto.getSurveyId()).orElseThrow(()->new NullPointerException("예외가 발생하였습니다."));
        if(answerRepository.findByUserAndSurvey(user,survey).isPresent()){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        } // 이미 선택한 설문지를 중복 응답 시 에러 출력
        if(survey.getMaxChoice() < requestDto.getAnswer()){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        } // 선택지에 없는 응답 시 에러 출력
        Answer answer = new Answer(requestDto.getAnswer(), user,survey);
        if(survey.getDeadline().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        }
        Answer savedAnswer = answerRepository.save(answer);
        MessageDto message = new MessageDto("작성이 완료되었습니다.");
        return message;
    }

    public List<AnswerResponseDto> getAnswers(User user) {
        List<Answer> answerList = answerRepository.findAllByUser(user);
        return answerList.stream().map(i-> new AnswerResponseDto(i)).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MessageDto updateAnswer(AnswerRequestDto requestDto,Long answerId, User user) {
        Answer answer = answerRepository.findById(answerId).orElseThrow(()->new NullPointerException("예외가 발생하였습니다."));
//        Answer answer = answerRepository.findByIdForUpdate(answerId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 대한 답변을 찾을 수 없습니다."));

        if (!answer.getUser().getUserId().equals(user.getUserId())){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        } // 사용자가 응답자가 아닐 시 에러 출력
        if(answer.getSurvey().getMaxChoice() < requestDto.getAnswer()){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        } // 선택지에 없는 응답으로 변경 시 에러 출력
        answer.update(requestDto.getAnswer());
        MessageDto message = new MessageDto("수정이 완료되었습니다.");
        return message;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MessageDto deleteAnswer(Long answerId, User user) {
        Answer answer = answerRepository.findById(answerId).orElseThrow(()->new NullPointerException("예외가 발생하였습니다."));
//         Pessimistic Locking 적용
//        Answer answer = answerRepository.findByIdForUpdate(answerId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 대한 답변을 찾을 수 없습니다."));

        if (!answer.getUser().getUserId().equals(user.getUserId())){
            throw new IllegalArgumentException("예외가 발생하였습니다.");
        } // 사용자가 응답자가 아닐 시 에러 출력
        answerRepository.delete(answer);
        MessageDto message = new MessageDto("삭제가 완료되었습니다.");
        return message;
    }
}
