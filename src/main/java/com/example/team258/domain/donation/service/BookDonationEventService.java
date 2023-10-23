package com.example.team258.domain.donation.service;

import com.example.team258.common.dto.MessageDto;
import com.example.team258.common.entity.Book;
import com.example.team258.common.entity.BookStatusEnum;
import com.example.team258.common.entity.User;
import com.example.team258.common.jwt.SecurityUtil;
import com.example.team258.common.repository.BookRepository;
import com.example.team258.domain.donation.dto.*;
import com.example.team258.domain.donation.entity.BookApplyDonation;
import com.example.team258.domain.donation.entity.BookDonationEvent;
import com.example.team258.domain.donation.entity.QBookDonationEvent;
import com.example.team258.domain.donation.repository.BookApplyDonationRepository;
import com.example.team258.domain.donation.repository.BookDonationEventRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookDonationEventService {
    private final BookDonationEventRepository bookDonationEventRepository;
    private final BookRepository bookRepository;
    private final BookApplyDonationRepository bookApplyDonationRepository;

    @Transactional
    public ResponseEntity<MessageDto> createDonationEvent(BookDonationEventRequestDto bookDonationEventRequestDto) {
        BookDonationEvent bookDonationEvent = new BookDonationEvent(bookDonationEventRequestDto);
        bookDonationEventRepository.save(bookDonationEvent);
        return ResponseEntity.ok(new MessageDto("이벤트추가가 완료되었습니다"));
    }


    @Transactional
    public ResponseEntity<MessageDto> updateDonationEvent(Long donationId, BookDonationEventRequestDto bookDonationEventRequestDto) {
        BookDonationEvent bookDonationEvent = bookDonationEventRepository.findById(donationId).orElseThrow(
                () -> new IllegalArgumentException("해당 이벤트가 존재하지 않습니다.")
        );
        bookDonationEvent.update(bookDonationEventRequestDto);
        return ResponseEntity.ok(new MessageDto("이벤트 수정이 완료되었습니다"));
    }

    @Transactional
    public ResponseEntity<MessageDto> deleteDonationEvent(Long donationId) {
        User user = SecurityUtil.getPrincipal().get();
        if(!user.getRole().getAuthority().equals("ROLE_ADMIN")){
            return ResponseEntity.badRequest().body(new MessageDto("관리자만 이벤트를 삭제할 수 있습니다."));
        }
        BookDonationEvent bookDonationEvent = bookDonationEventRepository.findById(donationId).orElseThrow(
                () -> new IllegalArgumentException("해당 이벤트가 존재하지 않습니다.")
        );
        /**
         * 연관 관계 삭제
         * 도서와 나눔 이벤트 간의 연관 관계 삭제
         */
        int bookSize = bookDonationEvent.getBooks().size();
        for (int i = bookSize - 1; i >= 0; i--) {
            bookDonationEvent.getBooks().get(i).changeStatus(BookStatusEnum.POSSIBLE);
            bookDonationEvent.removeBook(bookDonationEvent.getBooks().get(i));
        }

        /**
         * 도서와 나눔 신청 간의 연관 관계 삭제
         */
        int applysize = bookDonationEvent.getBookApplyDonations().size();
        for (int i = applysize - 1; i >= 0; i--) {
            Book book = bookRepository.findById(bookDonationEvent.getBookApplyDonations().get(i).getBook().getBookId()).orElseThrow(
                    () -> new IllegalArgumentException("해당 책이 존재하지 않습니다.")
            );
            BookApplyDonation bookApplyDonation = bookApplyDonationRepository.findById(bookDonationEvent.getBookApplyDonations().get(i).getApplyId()).orElseThrow(
                    () -> new IllegalArgumentException("해당 신청이 존재하지 않습니다.")
            );
            bookApplyDonation.removeBook(book);
            int a;
        }

        bookDonationEventRepository.delete(bookDonationEvent);
        return ResponseEntity.ok(new MessageDto("이벤트 삭제가 완료되었습니다"));
    }

    public List<BookDonationEventResponseDto> getDonationEvent() {
        return bookDonationEventRepository.findAll().stream()
                .map(BookDonationEventResponseDto::new)
                .toList();
    }

    public BookDonationEventPageResponseDto getDonationEventV2(Pageable pageable) {
         Page<BookDonationEvent> bookDonationEvents =bookDonationEventRepository.findAll(pageable);
         int totalPages = bookDonationEvents.getTotalPages();
         List<BookDonationEventResponseDto> bookDonationEventResponseDtos = bookDonationEvents.stream().map(BookDonationEventResponseDto::new).toList();

         return new BookDonationEventPageResponseDto(bookDonationEventResponseDtos, totalPages);
    }


    public BookDonationEventPageResponseDtoV3 getDonationEventV3(Pageable pageable) {
        Page<BookDonationEvent> bookDonationEvents =bookDonationEventRepository.findAll(pageable);
        int totalPages = bookDonationEvents.getTotalPages();
        List<BookDonationEventResponseDtoV3> bookDonationEventResponseDtos = bookDonationEvents.stream().map((BookDonationEvent t) -> new BookDonationEventResponseDtoV3(t)).toList();

        return new BookDonationEventPageResponseDtoV3(bookDonationEventResponseDtos, totalPages);
    }


    public BookDonationEventOnlyPageResponseDto getDonationEventOnlyV2(PageRequest pageRequest) {
        Page<BookDonationEvent> bookDonationEvents =bookDonationEventRepository.findAll(pageRequest);
        int totalPages = bookDonationEvents.getTotalPages();
        List<BookDonationEventOnlyResponseDto> bookDonationEventResponseDtos = bookDonationEvents.stream().map(BookDonationEventOnlyResponseDto::new).toList();
        return new BookDonationEventOnlyPageResponseDto(bookDonationEventResponseDtos, totalPages);

    }

    public BookDonationEventOnlyPageResponseDto getDonationEventOnlyV3(PageRequest pageRequest, Long donationId, LocalDate eventStartDate, LocalDate eventEndDate) {
        QBookDonationEvent qBookDonationEvent = QBookDonationEvent.bookDonationEvent;
        BooleanBuilder builder = new BooleanBuilder();

        if(donationId!=null)
            builder.and(qBookDonationEvent.donationId.eq(donationId));
        if(eventStartDate!=null)
            builder.and(qBookDonationEvent.createdAt.after(eventStartDate.atStartOfDay()));
        if(eventEndDate!=null)
            builder.and(qBookDonationEvent.closedAt.before(eventEndDate.atStartOfDay()));

        Page<BookDonationEvent> bookDonationEvents =bookDonationEventRepository.findAll(builder,pageRequest);
        int totalPages = bookDonationEvents.getTotalPages();
        List<BookDonationEventOnlyResponseDto> bookDonationEventResponseDtos = bookDonationEvents.stream().map(BookDonationEventOnlyResponseDto::new).toList();
        return new BookDonationEventOnlyPageResponseDto(bookDonationEventResponseDtos, totalPages);

    }
    public List<BookDonationEventResponseDto> getDonationEventPage() {
        return bookDonationEventRepository.findAll().stream()
                .map(BookDonationEventResponseDto::new)
                .toList();
    }

    @Transactional
    public MessageDto settingDonationEvent(BookDonationSettingRequestDto bookDonationSettingRequestDto) {
        BookDonationEvent bookDonationEvent = bookDonationEventRepository.findById(bookDonationSettingRequestDto.getDonationId()).orElseThrow(
                () -> new IllegalArgumentException("해당 이벤트가 존재하지 않습니다.")
        );
        List<Book> books =bookDonationSettingRequestDto.getBookIds().stream()
                .map(bookId -> bookRepository.findById(bookId).orElseThrow(
                        () -> new IllegalArgumentException("해당 책이 존재하지 않습니다.")
                ))
                .toList();

        books.forEach(book -> {
            bookDonationEvent.addBook(book);
            book.changeStatus(BookStatusEnum.DONATION);
        });

        return new MessageDto("이벤트 설정이 완료되었습니다");
    }

    @Transactional
    public MessageDto settingCancelDonationEvent(BookDonationSettingCancelRequestDto bookDonationSettingCancelRequestDto) {
        BookDonationEvent bookDonationEvent = bookDonationEventRepository.findById(bookDonationSettingCancelRequestDto.getDonationId()).orElseThrow(
                () -> new IllegalArgumentException("해당 이벤트가 존재하지 않습니다.")
        );
        Book book =bookRepository.findById(bookDonationSettingCancelRequestDto.getBookId()).orElseThrow(
                () -> new IllegalArgumentException("해당 책이 존재하지 않습니다.")
        );
        bookDonationEvent.removeBook(book);
        book.changeStatus(BookStatusEnum.POSSIBLE);
        return new MessageDto("해당 도서가 이벤트에서 삭제 완료되었습니다");
    }

    @Transactional
    public MessageDto endDonationEvent(Long donationId) {
        User user = SecurityUtil.getPrincipal().get();
        if(!user.getRole().getAuthority().equals("ROLE_ADMIN")){
            return new MessageDto("관리자만 이벤트를 종료할 수 있습니다.");
        }
        BookDonationEvent bookDonationEvent = bookDonationEventRepository.findById(donationId).orElseThrow(
                () -> new IllegalArgumentException("해당 이벤트가 존재하지 않습니다.")
        );
        bookDonationEventRepository.delete(bookDonationEvent);
        return new MessageDto("이벤트 종료가 완료되었습니다");
    }


}