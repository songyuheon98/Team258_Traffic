package com.example.team258.service;

import com.example.team258.dto.MessageDto;
import com.example.team258.entity.Book;
import com.example.team258.entity.BookRent;
import com.example.team258.entity.BookStatus;
import com.example.team258.entity.User;
import com.example.team258.repository.BookRentRepository;
import com.example.team258.repository.BookRepository;
import com.example.team258.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookRentService {
    private final BookRentRepository bookRentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto createRental(Long bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->new IllegalArgumentException("book을 찾을 수 없습니다."));
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(()->new IllegalArgumentException("user를 찾을 수 없습니다."));
        if (book.getBookStatus() != BookStatus.POSSIBLE) {
            throw new IllegalArgumentException("책이 대여 가능한 상태가 아닙니다.");
        }
        book.changeStatus(BookStatus.IMPOSSIBLE);
        BookRent bookRent = bookRentRepository.save(new BookRent(book));
        savedUser.addBookRent(bookRent);

        return new MessageDto("도서 대출 신청이 완료되었습니다");
    }

    @Transactional
    public MessageDto deleteRental(Long bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->new IllegalArgumentException("book을 찾을 수 없습니다."));
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(()->new IllegalArgumentException("user를 찾을 수 없습니다."));
        BookRent bookRent = book.getBookRent();
        if (bookRent == null) {
            throw new IllegalArgumentException("해당 책은 대여중이 아닙니다.");
        }
        if (!savedUser.getBookRents().contains(bookRent)) {
            throw new IllegalArgumentException("해당 책을 대여중이 아닙니다.");
        }
        bookRentRepository.deleteById(bookRent.getBookRentId()); //이거만 삭제해도 되는지 확인필요
        book.changeStatus(BookStatus.POSSIBLE);

        return new MessageDto("도서 반납이 완료되었습니다");
    }
}
