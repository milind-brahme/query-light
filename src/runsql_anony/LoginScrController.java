package runsql_anony;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginScrController implements Initializable{
@FXML private Text actionTarget;
@FXML private ComboBox comboAllDBList;
@FXML private TableView tabDBList;
@FXML private TextField userName;
@FXML private PasswordField password;
@FXML private TextArea jdbcUrl;

private Bms_Constants bc;
private TableColumn colDatabase;
private TableColumn colSchema;
private ObservableList<DbList> dblist;
private    StoredDbDetails sdd  = new StoredDbDetails();;

    private void tbl_dblistProcessChange() {
    System.out.println("Inside table change");
        try {
            int sel_idx = tabDBList.getSelectionModel().getSelectedIndex();
            String usr = ((DbList) tabDBList.getSelectionModel().getSelectedItem()).dbUser;
            String db =((DbList) tabDBList.getSelectionModel().getSelectedItem()).dbName;
            String pwd = sdd.get_stored_pwd(db.toUpperCase(), usr.toUpperCase());
            String jdbcString = sdd.get_stored_jdbcstr(db.toUpperCase(), usr.toUpperCase());
            if (db != null && pwd != null) {
                userName.setText(usr);
                password.setText(pwd);
                comboAllDBList.getSelectionModel().select (db);
                if(db.contains(":"))
                {
                    jdbcUrl.setText(jdbcString);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

private void buildTable()
{   colDatabase = new TableColumn("Database");
    colSchema = new TableColumn("User");
    colDatabase.setCellValueFactory(new PropertyValueFactory<DbList,String>("dbName"));
    colSchema.setCellValueFactory(new PropertyValueFactory<DbList,String>("dbUser"));
    dblist = FXCollections.observableArrayList();

    for (int i = 0; i < sdd.getDbRecordsCount(); i++) {
        dblist.add( new DbList(sdd.getDbRecordDatabaseNameAt(i), sdd.getDbRecordUserNameAt(i)));
    }
    tabDBList.getColumns().removeAll();
    tabDBList.setItems(dblist);
    tabDBList.getColumns().addAll(colDatabase,colSchema);
    tabDBList.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> tbl_dblistProcessChange());

}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bc = new Bms_Constants();
        System.out.println("Initalizing Login controller");
        comboAllDBList.setItems(FXCollections.observableArrayList(bc.get_db_names()));
        new AutoCompleteComboBoxListener<String>(comboAllDBList);
        buildTable();
    }

    public static class DbList {
        public String dbName;
        public String dbUser;

        public DbList(String dbName, String dbUser) {
            this.dbName = dbName;
            this.dbName = dbName;
            this.dbUser = dbUser;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getDbUser() {
            return dbUser;
        }

        public void setDbUser(String dbUser) {
            this.dbUser = dbUser;
        }

    }

}
