package devdragons.yiuServer.service;

import devdragons.yiuServer.domain.User;
import devdragons.yiuServer.domain.state.UserEntranceCategory;
import devdragons.yiuServer.domain.state.UserRoleCategory;
import devdragons.yiuServer.domain.state.UserStatusCategory;
import devdragons.yiuServer.domain.state.UserTrackCategory;
import devdragons.yiuServer.dto.request.UserRequestDto;
import devdragons.yiuServer.exception.CustomException;
import devdragons.yiuServer.exception.ErrorCode;
import devdragons.yiuServer.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class MainServiceTest {
    @InjectMocks
    private MainService mainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void 회원가입_성공() throws Exception {
        // given
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setId("202033009");
        userRequestDto.setName("김예서");
        userRequestDto.setPwd("1234");
        userRequestDto.setGrade(1);
        userRequestDto.setRole(UserRoleCategory.ADMIN);
        userRequestDto.setStatus(UserStatusCategory.STUDENT);
        userRequestDto.setMajor("33");
        userRequestDto.setDepartment("컴퓨터과학");
        userRequestDto.setTrack(UserTrackCategory.SINGLE);
        userRequestDto.setEntrance(UserEntranceCategory.FRESH);
        userRequestDto.setProfessor("이완주");

        // when
        mainService.register(userRequestDto);

        // then
        Optional<User> user = userRepository.findById(userRequestDto.getId());
        assertThat(user.isPresent());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 데이터 미입력")
    void 회원가입_실패_데이터_미입력() throws Exception {
        // given
        UserRequestDto userRequestDto = new UserRequestDto();

        // when & then
        CustomException customException = assertThrows(CustomException.class, () -> {mainService.register(userRequestDto);});
        assertEquals(ErrorCode.INSUFFICIENT_DATA, customException.getErrorCode());
        assertEquals(400, customException.getErrorCode().getStatus());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 데이터 중복")
    void 회원가입_실패_데이터_중복() throws Exception {
        // given
        UserRequestDto userRequestDto1 = new UserRequestDto();
        userRequestDto1.setId("202033009");
        userRequestDto1.setName("김예서");
        userRequestDto1.setPwd("1234");
        userRequestDto1.setGrade(1);
        userRequestDto1.setRole(UserRoleCategory.ADMIN);
        userRequestDto1.setStatus(UserStatusCategory.STUDENT);
        userRequestDto1.setMajor("33");
        userRequestDto1.setDepartment("컴퓨터과학");
        userRequestDto1.setTrack(UserTrackCategory.SINGLE);
        userRequestDto1.setEntrance(UserEntranceCategory.FRESH);
        userRequestDto1.setProfessor("이완주");

        // 첫 번째 회원가입 시에는 중복이 없으므로 false
        when(userRepository.existsById(userRequestDto1.getId())).thenReturn(false);
        mainService.register(userRequestDto1);

        // 두 번째 회원가입 시에는 중복이 발생해야 하므로 true로 설정
        when(userRepository.existsById(userRequestDto1.getId())).thenReturn(true);

        // when & then
        CustomException customException = assertThrows(CustomException.class, () -> {
            mainService.register(userRequestDto1);
        });

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE);
        assertThat(customException.getErrorCode().getStatus()).isEqualTo(409);
    }


    @Test
    @DisplayName("회원가입 시 이메일 전송 테스트")
    void 회원가입_시_이메일_전송_테스트() throws Exception {
        // given
        String id = "202033009";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        int authNum = mainService.sendEmailWhenRegister(id);

        // then
        assertThat(authNum).isGreaterThanOrEqualTo(100000).isLessThanOrEqualTo(999999);
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}