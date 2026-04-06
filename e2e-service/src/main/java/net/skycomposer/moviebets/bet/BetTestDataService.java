package net.skycomposer.moviebets.bet;

import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.testdata.JdbcTestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BetTestDataService extends JdbcTestDataService {

    @Autowired
    @Qualifier("betJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public void resetDatabase() {
        executeString("TRUNCATE bet");
        executeString("TRUNCATE market_settle_status");
        executeString("TRUNCATE market_open_status");
        executeString("TRUNCATE bet_settle_request");
        executeString("TRUNCATE user_item_status");
        executeString("TRUNCATE user_item_votes");
        executeString("TRUNCATE user_friend_weight");
    }

}
