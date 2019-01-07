package datasource;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Created by panjiannan on 2018/8/4.
 */
public class SpringJDBCTest {

    @Test
    public void test() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://localhost:3306/test?characterEncoding=utf-8", "root", "123456");
        JdbcTemplate template = new JdbcTemplate(dataSource);
        String sql = "SELECT * FROM test_table";

        List<Map<String, Object>> list = template.queryForList(sql);
        System.out.println(JSONObject.toJSONString(list));

        String sql2 = "UPDATE test_table SET name ='潘建南' WHERE id =1";
        Boolean result = template.execute(new ExecuteStatementCallback(sql2));
        System.out.println(result);

        List<Map<String, Object>> list2 = template.queryForList(sql);
        System.out.println(JSONObject.toJSONString(list2));


    }

    class ExecuteStatementCallback implements StatementCallback<Boolean>, SqlProvider {

        private String sql;

        public ExecuteStatementCallback(String sql) {
            this.sql = sql;
        }

        @Override
        public String getSql() {
            return sql;
        }

        @Override
        public Boolean doInStatement(Statement statement) throws SQLException, DataAccessException {
            System.out.println("auto:" + statement.getConnection().getAutoCommit());

            return statement.execute(sql);
        }
    }
}

