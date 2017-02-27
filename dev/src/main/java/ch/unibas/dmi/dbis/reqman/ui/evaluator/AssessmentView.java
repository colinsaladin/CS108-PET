package ch.unibas.dmi.dbis.reqman.ui.evaluator;

import ch.unibas.dmi.dbis.reqman.core.Group;
import ch.unibas.dmi.dbis.reqman.core.Milestone;
import ch.unibas.dmi.dbis.reqman.core.Progress;
import ch.unibas.dmi.dbis.reqman.ui.common.Utils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class AssessmentView extends BorderPane implements PointsChangeListener {

    private HBox titleBar;
    private AnchorPane titleAnchor;
    private Label lblChoice;
    private ComboBox<Milestone> cbMilestones;
    private Button btnRefresh;
    private HBox statusWrapper;
    private AnchorPane statusBar;
    private Label lblSum;
    private TextField tfSum;
    private VBox content;
    private ScrollPane scrollPane;


    private EvaluatorController controller;

    private Group group;

    public AssessmentView(EvaluatorController controller, Group active){
        super();

        this.controller = controller;
        this.group = active;

        initComponents();
        layoutComponents();
        loadGroup();

        updateProgressViews();
    }

    private void loadGroup() {
        // TODO load group really
        setupProgressMap();
    }

    /**
     * Maps MS ordinal to a map of Req.name <-> Progress (obj)
     */
    private Map<Integer, Map<String, Progress>> progressMap = new TreeMap<>();

    /**
     * Sets up the map as if the group was newly created
     */
    private void setupProgressMap() {
        controller.getMilestones().forEach(ms -> {
            TreeMap<String, Progress> reqProgMap = new TreeMap<String, Progress>();
            controller.getRequirementsByMilestone(ms.getOrdinal()).forEach(r -> {
                reqProgMap.put(r.getName(), new Progress(r.getName(), ms.getOrdinal(), 0));
            });
            progressMap.put(ms.getOrdinal(), reqProgMap);
        });
    }

    private void initComponents(){
        titleBar = new HBox();
        titleAnchor = new AnchorPane();
        lblChoice = new Label("Current Milestone: ");
        cbMilestones = new ComboBox<>();
        btnRefresh = new Button("Update");
        statusWrapper = new HBox();
        statusBar = new AnchorPane();
        lblSum = new Label("Sum:");
        tfSum = new TextField();
        tfSum.setEditable(false);
        content = new VBox();
        scrollPane = new ScrollPane();
    }

    private void layoutComponents(){
        // Forge top aka title bar:
        titleBar.getChildren().addAll(lblChoice, cbMilestones, btnRefresh );

        if(controller != null){
            cbMilestones.setItems(FXCollections.observableList(controller.getMilestones()));
            cbMilestones.setCellFactory(param -> new Utils.MilestoneCell());
            cbMilestones.setButtonCell(new Utils.MilestoneCell());
        }

        if(controller!=null){
            btnRefresh.setOnAction(this::updateProgressViews);
        }

        VBox titleWrapper = new VBox();
        Separator sep = new Separator();
        titleWrapper.getChildren().addAll(titleBar, sep);
        setTop(titleWrapper);

        // Forge center aka ProgressView list
        scrollPane.setContent(content);
        setCenter(scrollPane);

        // Forge bottom aka status bar:
        statusWrapper.getChildren().addAll(lblSum, tfSum);
        Separator sep2 = new Separator();
        statusBar.getChildren().add(statusWrapper);
        statusBar.getChildren().add(sep2);
        AnchorPane.setTopAnchor(sep2, 0d);
        AnchorPane.setRightAnchor(statusWrapper, 10d);
        setBottom(statusBar);

        cbMilestones.getSelectionModel().select(0);

    }

    private List<ProgressView> activeProgressViews = new ArrayList<>();

    private void loadActiveProgressViews(){
        Milestone activeMS = cbMilestones.getSelectionModel().getSelectedItem();
        if(activeMS == null){
            return;
        }
        activeProgressViews.clear();
        controller.getRequirementsByMilestone(activeMS.getOrdinal()).forEach(r ->{
            Progress p = progressMap.get(activeMS.getOrdinal()).get(r.getName());
            ProgressView pv = new ProgressView(p,r);
            pv.addPointsChangeListener(this);
            activeProgressViews.add(pv);
        });

    }

    private void updateProgressViews(){
        detachProgressViews();
        tfSum.setText(String.valueOf(0));
        loadActiveProgressViews();
        attachProgressViews();
        calcActiveSum();
    }

    private void calcActiveSum() {
        ArrayList<Double> currentPoints = new ArrayList<>();
        activeProgressViews.forEach(pv -> {
            currentPoints.add(pv.getProgress().getPoints() );
        });
        double sum = currentPoints.stream().mapToDouble(Double::doubleValue).sum();
        tfSum.setText(String.valueOf(sum));
    }

    private void updateProgressViews(ActionEvent event){
        updateProgressViews();
    }

    private void attachProgressViews(){
        activeProgressViews.forEach(pv -> {
            addProgressView(pv);
        });
    }

    private void detachProgressViews(){
        activeProgressViews.forEach(pv -> removeProgressView(pv));
    }

    private void addProgressView(ProgressView pv){
        content.getChildren().add(pv);
        pv.prefWidthProperty().bind(scrollPane.widthProperty() );
    }

    private void removeProgressView(ProgressView pv){
        content.getChildren().remove(pv);
    }

    public void bindToParentSize(Region parent){
        prefWidthProperty().bind(parent.widthProperty());
        prefHeightProperty().bind(parent.heightProperty());
    }

    private void bindContent(){
        content.prefWidthProperty().bind(widthProperty());
        content.prefHeightProperty().bind(heightProperty());
    }

    @Override
    public void pointsChanged(double newValue) {
        calcActiveSum();
    }
}
