package fu.se.myplatform.service;

import fu.se.myplatform.repository.LoginLogRepository;
import fu.se.myplatform.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogReportService {
    @Autowired
    private LoginLogRepository loginLogRepository;
    @Autowired
    private ErrorLogRepository errorLogRepository;

    public long getLoginCount() {
        return loginLogRepository.countByIsRegister(false);
    }

    public long getRegisterCount() {
        return loginLogRepository.countByIsRegister(true);
    }

    public long getErrorCount() {
        return errorLogRepository.count();
    }
}

