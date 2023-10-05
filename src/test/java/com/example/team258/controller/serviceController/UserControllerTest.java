//package com.example.team258.controller.serviceController;
//
//import com.example.team258.dto.UserSignupRequestDto;
//import com.example.team258.entity.MessageDto;
//import com.example.team258.entity.UserRoleEnum;
//import com.example.team258.service.UserService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.seoduct.logging.Message;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@SpringBootTest
//@AutoConfigureTestDatabase
//@AutoConfigureMockMvc(addFilters = false)
//class UserControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserService userService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    @Test
//    void 사용자_회원가입_테스트() throws Exception {
//        // given
//        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
//                .username("bin0222")
//                .password1("Bin@12345")
//                .password2("Bin@12345")
//                .build();
//
//        MessageDto msg =  MessageDto.builder()
//                .msg("회원가입 성공")
//                .build();
//
//        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(new ResponseEntity<>(msg, HttpStatus.OK));
//
//        // when
//        // then
//        mockMvc.perform(post("/api/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("회원가입 성공"));
//    }
//
//    @Test
//    void escape() {
//    }
//}