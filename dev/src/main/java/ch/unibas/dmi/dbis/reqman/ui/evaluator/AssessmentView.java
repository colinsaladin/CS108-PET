package ch.unibas.dmi.dbis.reqman.ui.evaluator;

import ch.unibas.dmi.dbis.reqman.core.*;
import ch.unibas.dmi.dbis.reqman.ui.common.Utils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

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
    private Button btnSummary;
    private HBox statusWrapper;
    private AnchorPane statusBar;
    private Label lblSum;
    private TextField tfSum;
    private VBox content;
    private ScrollPane scrollPane;


    private EvaluatorController controller;

    private Group group;
    private Milestone activeMS = null;

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
        // TODO Load summary

        List<Progress> progressList = group.getProgressList();
        if(progressList == null || progressList.isEmpty() ){
            setupProgressMap();
        }else{
            loadProgress(progressList);
        }

    }

    private void loadProgress(List<Progress> list) {
        for(Progress p : list){
            int ordinal = p.getMilestoneOrdinal();
            String reqName = p.getRequirementName();

            if(progressMap.containsKey(ordinal) ){
                // MS entry exists already
                Map<String, Progress> rpMap = progressMap.get(ordinal);
                if(rpMap == null || rpMap.containsKey(reqName) ){
                    // no map, but ordinal OR requirement is already existing. THIS IS A SEVERE ERROR
                }else{
                    rpMap.put(reqName, p);
                }
            }else{
                // FIRST time this MS occurs:
                TreeMap<String, Progress> rpMap = new TreeMap<>();
                rpMap.put(reqName, p);
                progressMap.put(ordinal, rpMap);
            }
        }
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
        btnSummary = new Button("Comments");
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
        titleBar.getChildren().addAll(lblChoice, cbMilestones, btnRefresh, btnSummary );
        titleBar.setStyle(titleBar.getStyle()+" -fx-spacing: 10px");

        if(controller != null){
            cbMilestones.setItems(FXCollections.observableList(controller.getMilestones()));
            cbMilestones.setCellFactory(param -> new Utils.MilestoneCell());
            cbMilestones.setButtonCell(new Utils.MilestoneCell());
        }

        if(controller!=null){
            btnRefresh.setOnAction(this::updateProgressViews);
            btnSummary.setOnAction(this::handleComments);
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

    private List<ProgressSummary> summaries = new ArrayList<>();

    private void handleComments(ActionEvent event) {
        Milestone ms = cbMilestones.getSelectionModel().getSelectedItem();
        if(ms != null){
            ProgressSummary ps = null;
            boolean replace = false;
            if(hasSummaryForMilestone(ms)){
                ps = EvaluatorPromptFactory.promptSummary(ms, group.getName(), getSummaryForMilestone(ms));
                replace = true;
            } else{
                ps = EvaluatorPromptFactory.promptSummary(ms, group.getName() );
            }
            if(ps != null){
                if(replace){
                    summaries.remove(getSummaryForMilestone(ms));
                }
                summaries.add(ps);
            }
        }
    }

    private boolean hasSummaryForMilestone(Milestone ms){
        if(summaries.isEmpty() ){
            return false;
        }else{
            for(ProgressSummary ps:summaries){
                if(ps.getMilestoneOrdinal() == ms.getOrdinal()){
                    return true;
                }
            }
        }
        return false;
    }

    private ProgressSummary getSummaryForMilestone(Milestone ms){
        if(summaries.isEmpty() ){
            return null;
        }else{
            for(ProgressSummary ps:summaries){
                if(ps.getMilestoneOrdinal() == ms.getOrdinal() ){
                    return ps;
                }
            }
        }
        return null;
    }

    public List<ProgressSummary> getSummaries(){
        return summaries;
    }

    private List<ProgressView> activeProgressViews = new ArrayList<>();

    private void loadActiveProgressViews(){
        Milestone activeMS = cbMilestones.getSelectionModel().getSelectedItem();
        if(activeMS == null){
            return;
        }
        this.activeMS = activeMS;
        visitedMilestones.add(activeMS);
        activeProgressViews.clear();
        controller.getRequirementsByMilestone(activeMS.getOrdinal()).forEach(r ->{
            Progress p = progressMap.get(activeMS.getOrdinal()).get(r.getName());
            ProgressView pv = new ProgressView(p,r);
            pv.addPointsChangeListener(this);
            activeProgressViews.add(pv);
        });

    }

    private Set<Milestone> visitedMilestones = new HashSet<>();

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
            Requirement req = pv.getRequirement();
            double factor = req.isMalus() ? -1.0 : 1.0;
            currentPoints.add(pv.getProgress().getPoints() * factor);
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
        controller.markDirty(getActiveGroup() );
    }

    public Group getActiveGroup(){
        return group;
    }

    /**
     * ONLY if group has to be saved. Grabs ALL progress objects
     *
     * @return
     */
    public List<Progress> getProgressListForSaving(){
        List<Progress> list = new ArrayList<>();

        progressMap.values().forEach(consumer -> consumer.values().forEach(list::add));

        return list;
    }


}
