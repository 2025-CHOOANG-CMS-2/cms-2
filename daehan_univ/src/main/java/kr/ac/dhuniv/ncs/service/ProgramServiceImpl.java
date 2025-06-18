package kr.ac.dhuniv.ncs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import kr.ac.dhuniv.ncs.domain.ProgramListView;
import kr.ac.dhuniv.ncs.repository.ProgramListViewRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramListViewRepository programListViewRepository;

    @Override
    public List<ProgramListView> getAllPrograms() {
        return programListViewRepository.findAll();
    }
}
