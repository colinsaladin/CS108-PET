package ch.unibas.dmi.dbis.reqman.ui.evaluator;

import ch.unibas.dmi.dbis.reqman.common.StringUtils;
import ch.unibas.dmi.dbis.reqman.core.Catalogue;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class CatalogueInfoView extends HBox {

    private GridPane root = new GridPane();


    private Label lblName, lblLecture, lblSemester, lblDescription, lblSum;

    private TextField tfName, tfLecture, tfSemester, tfSum;
    private TextArea taDesc;

    private Catalogue catalogue;

    public CatalogueInfoView() {
        this(null);
    }

    public CatalogueInfoView(Catalogue catalogue) {
        super();
        this.catalogue = catalogue;

        initComponents();

        getChildren().add(root);

        displayData();
    }

    public void setCatalogue(Catalogue cat) {
        this.catalogue = cat;
    }

    public void displayData() {
        displayData(catalogue);
    }

    public void displayData(Catalogue catalogue) {
        if (catalogue != null) {
            tfName.setText(catalogue.getName());
            tfLecture.setText(catalogue.getLecture());
            tfSemester.setText(catalogue.getSemester());
            taDesc.setText(catalogue.getDescription());
            tfSum.setText(StringUtils.prettyPrint(catalogue.getSum()));
        }
    }

    private void initComponents() {
        setStyle("-fx-padding: 10px; -fx-spacing: 10px");
        lblName = new Label("Name");
        lblLecture = new Label("Lecture");
        lblSemester = new Label("Semester");
        lblDescription = new Label("Description");
        lblSum = new Label("Maximal Points");

        tfName = new TextField();
        tfName.setEditable(false);
        tfLecture = new TextField();
        tfLecture.setEditable(false);
        tfSemester = new TextField();
        tfSemester.setEditable(false);
        tfSum = new TextField();
        tfSum.setEditable(false);

        taDesc = new TextArea();
        taDesc.setEditable(false);

        int rowIndex = 0;

        root.add(lblName, 0, rowIndex);
        GridPane.setHgrow(lblName, Priority.ALWAYS);
        root.add(tfName, 1, rowIndex++);
        root.add(lblLecture, 0, rowIndex);
        GridPane.setHgrow(lblLecture, Priority.ALWAYS);
        root.add(tfLecture, 1, rowIndex++);
        root.add(lblSemester, 0, rowIndex);
        GridPane.setHgrow(lblSemester, Priority.ALWAYS);
        root.add(tfSemester, 1, rowIndex++);
        root.add(lblSum, 0, rowIndex);
        GridPane.setHgrow(lblSum, Priority.ALWAYS);
        root.add(tfSum, 1, rowIndex++);
        root.add(lblDescription, 0, rowIndex);
        GridPane.setHgrow(lblDescription, Priority.ALWAYS);
        root.add(taDesc, 1, rowIndex++);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);

        root.getColumnConstraints().addAll(col1, col2);
    }


}