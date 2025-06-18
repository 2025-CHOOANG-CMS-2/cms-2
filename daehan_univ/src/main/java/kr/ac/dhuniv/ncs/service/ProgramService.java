package kr.ac.dhuniv.ncs.service;

import kr.ac.dhuniv.ncs.domain.ProgramListView;

import java.util.List;

public interface ProgramService {
    List<ProgramListView> getAllPrograms();
}
