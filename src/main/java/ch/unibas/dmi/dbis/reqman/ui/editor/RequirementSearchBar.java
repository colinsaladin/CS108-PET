package ch.unibas.dmi.dbis.reqman.ui.editor;

import ch.unibas.dmi.dbis.reqman.control.EntityController;
import ch.unibas.dmi.dbis.reqman.data.Requirement;
import ch.unibas.dmi.dbis.reqman.ui.common.Utils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
public class RequirementSearchBar extends HBox {
  
  private final EditorHandler editorHandler;
  private Label nameLbl;
  private Label containsLbl;
  private Label infoLbl;
  private ComboBox<Mode> modeCB;
  private TextField searchInput;
  private Button filterBtn;
  private Button resetBtn;
  private Button closeBtn;
  
  public RequirementSearchBar(EditorHandler editorHandler) {
    this.editorHandler = editorHandler;
    initComponents();
    layoutComponents();
  }
  
  public void clear() {
    searchInput.clear();
  }
  
  private void layoutComponents() {
    getChildren().addAll(nameLbl, modeCB, containsLbl, searchInput, filterBtn, resetBtn, Utils.createHFill(), infoLbl, closeBtn);
    
    Utils.applyDefaultSpacing(this);
    getStyleClass().add("darkened");
  }
  
  private void initComponents() {
    nameLbl = new Label("Filter requirement");
    containsLbl = new Label("containing");
    infoLbl = new Label();
    modeCB = new ComboBox<>();
    modeCB.setItems(FXCollections.observableArrayList(Mode.values()));
    modeCB.getSelectionModel().select(Mode.TEXT);
    searchInput = new TextField();
    filterBtn = new Button("Filter");
    filterBtn.setOnAction(this::handleFilter);
    resetBtn = new Button("Reset");
    resetBtn.setOnAction(this::handleReset);
    closeBtn = new Button("Close");
    closeBtn.setOnAction(this::handleClose);
  }
  
  private void handleClose(ActionEvent actionEvent) {
    editorHandler.closeFilterBar();
  }
  
  private void handleReset(ActionEvent actionEvent) {
    editorHandler.displayAllRequirements();
  }
  
  private void handleFilter(ActionEvent actionEvent) {
    String pattern = searchInput.getText();
    if (StringUtils.isNotBlank(pattern)) {
      List<Requirement> filtered = null;
      
      switch (modeCB.getSelectionModel().getSelectedItem()) {
        case NAME:
          filtered = EntityController.getInstance().getCatalogueAnalyser().findRequirementsNameContains(pattern);
          break;
        case TEXT:
          filtered = EntityController.getInstance().getCatalogueAnalyser().findRequirementsContaining(pattern);
          break;
        case CATEGORY:
          filtered = EntityController.getInstance().getCatalogueAnalyser().findRequirementsForCategory(pattern);
          break;
      }
      if (filtered == null) {
        infoLbl.setText("No matches found");
        return;
      }
      infoLbl.setText(String.format("Showing %d matche(s)", filtered.size()));
      editorHandler.displayOnly(filtered);
    }
  }
  
  enum Mode {
    NAME,
    TEXT,
    CATEGORY;
    
    @Override
    public String toString() {
      return StringUtils.capitalize(name().toLowerCase());
    }
  }
}
