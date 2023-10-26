package com.example.team258.domain.donation.controller;

import com.example.team258.common.dto.BookResponseDto;
import com.example.team258.common.dto.MessageDto;
import com.example.team258.common.entity.BookStatusEnum;
import com.example.team258.domain.donation.dto.BookApplyDonationRequestDto;
import com.example.team258.domain.donation.dto.BookApplyDonationResponseDto;
import com.example.team258.domain.donation.service.BookApplyDonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class BookApplyDonationController {

    private final BookApplyDonationService bookApplyDonationService;
    private final Semaphore semaphore;
    @PostMapping("/bookApplyDonation")
    public ResponseEntity<MessageDto> createBookApplyDonation(@RequestBody BookApplyDonationRequestDto bookApplyDonationRequestDto){
        try{
            semaphore.acquire();
            return ResponseEntity.ok().body(bookApplyDonationService.createBookApplyDonation(bookApplyDonationRequestDto));
        }catch (InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageDto("나눔 신청에 실패했습니다."));
        }finally {
            semaphore.release();
        }
    }

    @DeleteMapping("/bookApplyDonation/{applyId}")
    public ResponseEntity<MessageDto> deleteBookApplyDonation(@PathVariable Long applyId){

        return ResponseEntity.ok().body(bookApplyDonationService.deleteBookApplyDonation(applyId));
    }

    /**
     * 책 기부 신청 목록 조회
     * @param bookStatus
     * @return
     */
    @GetMapping("/bookApplyDonation/books")
    public ResponseEntity<List<BookResponseDto>> getDonationBooks(@RequestParam BookStatusEnum bookStatus){
        return ResponseEntity.ok().body(bookApplyDonationService.getDonationBooks(bookStatus));
    }

    @GetMapping("/bookApplyDonation")
    public ResponseEntity<List<BookApplyDonationResponseDto>> getBookApplyDonations(){
        return ResponseEntity.ok().body(bookApplyDonationService.getBookApplyDonations());
    }

}

