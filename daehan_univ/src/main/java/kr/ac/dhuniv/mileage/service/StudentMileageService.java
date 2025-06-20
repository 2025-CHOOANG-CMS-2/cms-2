//package kr.ac.dhuniv.mileage.service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import kr.ac.dhuniv.mileage.dto.CmpInfoResponseDto;
//import kr.ac.dhuniv.mileage.repository.NcsCmpInfoRepository2;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class StudentMileageService {
//
//	private final NcsCmpInfoRepository2 cmpInfoRepository;
//
//    @Transactional(readOnly = true)
//    public List<CmpInfoResponseDto> getCompletedProgramsByStudent(Long stdId) {
//        return cmpInfoRepository.findByStudent_StdId(stdId)
//                .stream()
//                .map(CmpInfoResponseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//}
