package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.AppContext;

import uk.ac.ebi.pride.archive.dataprovider.utils.SubmissionTypeConstants;
import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;
import uk.ac.ebi.pride.gui.form.action.LoadSubmissionFileAction;
import uk.ac.ebi.pride.gui.form.comp.HeaderPanel;
import uk.ac.ebi.pride.gui.form.dialog.ResubmissionDialog;
import uk.ac.ebi.pride.toolsuite.gui.prop.PropertyChangeHelper;
import uk.ac.ebi.pride.gui.util.BorderUtil;
import uk.ac.ebi.pride.gui.util.ColourUtil;
import uk.ac.ebi.pride.gui.util.HttpUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This form allows to select the submission type
 *
 * NOTE: If user select a resubmission, it will prevent to change the submission after selecting resubmission.
 * They need to disable the resubmission in order to make a normal submission
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SubmissionTypeForm extends Form {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionTypeForm.class);

    private PropertyChangeBroadcaster propertyChangeBroadcaster = null;
    private TrainingModeCheckBoxController trainingModeCheckBoxController = new TrainingModeCheckBoxController();
    private JCheckBox trainingModeCheckBox = null;

    private static final String FULL_SUBMISSION_OPTION = "FULL_SUBMISSION";
    private static final String PARTIAL_SUBMISSION_OPTION = "PARTIAL_SUBMISSION";

    private static final String AFFINITY_SUBMISSION_OPTION = "AFFINITY_SUBMISSION";

    private String resubmissionChangeTypeError = "";

    private ResubmissionDialog resubmissionDialog;


    public SubmissionTypeForm() {
        propertyChangeBroadcaster = new PropertyChangeBroadcaster();
        initComponents();
    }

    private void initComponents() {
        // setup the main pane
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // create mission statement panel
        JPanel submissionOptionPanel = createSubmissionOptionPanel();
        this.add(submissionOptionPanel, BorderLayout.CENTER);

        // Training update
        trainingModeCheckBoxController.init(trainingModeCheckBox);
    }

    /**
     * Panel allows users to choose which type of submission they would like to perform
     */
    private JPanel createSubmissionOptionPanel() {
        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
//        optionPanel.setBorder(BorderUtil.createLoweredBorder());

        // title panel
        optionPanel.add(createOptionTitlePanel(), BorderLayout.NORTH);

        // button panel for choosing the submission type
        optionPanel.add(createOptionButtonPanel(), BorderLayout.CENTER);

        // external link panel
        optionPanel.add(createOptionLinkPanel(), BorderLayout.SOUTH);

        return optionPanel;
    }

    /**
     * Create a title panel for the option panel
     */
    private JPanel createOptionTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(appContext.getProperty("submission.type.before.start.title"));
        titleLabel.setFont(titlePanel.getFont().deriveFont(16f).deriveFont(Font.BOLD));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        trainingModeCheckBox = new JCheckBox();
        trainingModeCheckBox.setText(appContext.getProperty("training.mode.toggle.checkbox.text"));
        trainingModeCheckBox.addItemListener(new TrainingModeOptionListener());
        trainingModeCheckBox.setToolTipText(appContext.getProperty("training.mode.toggle.checkbox.help.text"));
        titlePanel.add(trainingModeCheckBox, BorderLayout.EAST);
//        secureCheckBox = new JCheckBox();
//        secureCheckBox.setText(appContext.getProperty("controlled.access.mode.toggle.checkbox.text"));
//        secureCheckBox.addItemListener(new ControlledAccessDataModeOptionListener());
//        secureCheckBox.setToolTipText(appContext.getProperty("controlled.access.toggle.checkbox.help.text"));
//        titlePanel.add(secureCheckBox, BorderLayout.EAST);
//        JLabel descPanel = new JLabel(appContext.getProperty("submission.type.before.start.desc"));
//        titlePanel.add(descPanel, BorderLayout.CENTER);
        titlePanel.add(Box.createRigidArea(new Dimension(10, 10)), BorderLayout.SOUTH);

        return titlePanel;
    }

    /**
     * Create a button panel for the option panel
     */
    private JPanel createOptionButtonPanel() {
        JPanel submissionOptionPanel = new JPanel();
        submissionOptionPanel.setBorder(BorderUtil.createLoweredBorder());
        GridLayout gridLayout = new GridLayout(3, 1);
        gridLayout.setVgap(10);
        submissionOptionPanel.setLayout(gridLayout);

        ButtonGroup buttonGroup = new ButtonGroup();
        // action listener
        ActionListener submissionOptionListener = new SubmissionOptionListener();


        // complete submission panel
        JPanel completeSubmissionPanel = new JPanel(new BorderLayout());
        HeaderPanel completeSubmissionHeaderPanel = new HeaderPanel();
        completeSubmissionHeaderPanel.setLayout(new BoxLayout(completeSubmissionHeaderPanel, BoxLayout.X_AXIS));
        completeSubmissionHeaderPanel.setPreferredSize(new Dimension(50, 20));

        completeSubmissionHeaderPanel.add(Box.createHorizontalGlue());
        JCheckBox completeSubmissionCheckBox = new JCheckBox();
        buttonGroup.add(completeSubmissionCheckBox);
        completeSubmissionCheckBox.setActionCommand(FULL_SUBMISSION_OPTION);
        completeSubmissionCheckBox.addActionListener(submissionOptionListener);
        completeSubmissionHeaderPanel.add(completeSubmissionCheckBox);
        completeSubmissionHeaderPanel.add(Box.createHorizontalGlue());
        completeSubmissionPanel.add(completeSubmissionHeaderPanel, BorderLayout.WEST);
        completeSubmissionCheckBox.doClick();

        String completeSubmissionTitle = appContext.getProperty("submission.type.full.submission.title");
        String completeSubmissionContent = appContext.getProperty("submission.type.full.submission.content");
        JPanel completeSubmissionContentPanel = createSubmissionOptionPanel(completeSubmissionTitle, completeSubmissionContent, true);
        completeSubmissionPanel.add(completeSubmissionContentPanel);
        submissionOptionPanel.add(completeSubmissionPanel);

        // partial submission panel
        JPanel partialSubmissionPanel = new JPanel(new BorderLayout());
        HeaderPanel partialSubmissionHeaderPanel = new HeaderPanel(new BorderLayout());
        partialSubmissionHeaderPanel.setLayout(new BoxLayout(partialSubmissionHeaderPanel, BoxLayout.X_AXIS));
        partialSubmissionHeaderPanel.setPreferredSize(new Dimension(50, 20));

        partialSubmissionHeaderPanel.add(Box.createHorizontalGlue());
        JCheckBox partialSubmissionCheckBox = new JCheckBox();
        buttonGroup.add(partialSubmissionCheckBox);
        partialSubmissionCheckBox.setActionCommand(PARTIAL_SUBMISSION_OPTION);
        partialSubmissionCheckBox.addActionListener(submissionOptionListener);
        partialSubmissionHeaderPanel.add(partialSubmissionCheckBox);
        partialSubmissionHeaderPanel.add(Box.createHorizontalGlue());
        partialSubmissionPanel.add(partialSubmissionHeaderPanel, BorderLayout.WEST);

        String partialSubmissionTitle = appContext.getProperty("submission.type.partial.submission.title");
        String partialSubmissionContent = appContext.getProperty("submission.type.partial.submission.content");
        JPanel partialSubmissionContentPanel = createSubmissionOptionPanel(partialSubmissionTitle, partialSubmissionContent, false);
        partialSubmissionPanel.add(partialSubmissionContentPanel);

        submissionOptionPanel.add(partialSubmissionPanel);

        // partial submission panel
        JPanel affinitySubmissionPanel = new JPanel(new BorderLayout());
        HeaderPanel affinityHeaderPanel = new HeaderPanel(new BorderLayout());
        affinityHeaderPanel.setLayout(new BoxLayout(affinityHeaderPanel, BoxLayout.X_AXIS));
        affinityHeaderPanel.setPreferredSize(new Dimension(50, 20));

        affinityHeaderPanel.add(Box.createHorizontalGlue());
        JCheckBox affinitySubmissionCheckBox = new JCheckBox();
        buttonGroup.add(affinitySubmissionCheckBox);
        affinitySubmissionCheckBox.setActionCommand(AFFINITY_SUBMISSION_OPTION);
        affinitySubmissionCheckBox.addActionListener(submissionOptionListener);
        affinityHeaderPanel.add(affinitySubmissionCheckBox);
        affinityHeaderPanel.add(Box.createHorizontalGlue());
        affinitySubmissionPanel.add(affinityHeaderPanel, BorderLayout.WEST);

        String affinitySubmissionTitle = appContext.getProperty("submission.type.affinity.submission.title");
        String affinitySubmissionContent = appContext.getProperty("submission.type.affinity.submission.content");
        JPanel affinitySubmissionContentPanel = createSubmissionOptionPanel(affinitySubmissionTitle, affinitySubmissionContent, false);
        affinitySubmissionPanel.add(affinitySubmissionContentPanel);

        submissionOptionPanel.add(affinitySubmissionPanel);

        return submissionOptionPanel;
    }

    public JPanel createSubmissionOptionPanel(String title, String content, boolean recommend) {
        JPanel contentPanel = new HeaderPanel();
        contentPanel.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel();
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        titleLabel.setText(title);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(5, 5)));
        if (recommend) {
            JLabel refLabel = new JLabel(" (recommended)");
            refLabel.setForeground(Color.green);
            refLabel.setFont(refLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));
            titlePanel.add(refLabel);
        }


        JLabel messageLabel = new JLabel();
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        messageLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        messageLabel.setVerticalTextPosition(SwingConstants.TOP);
        messageLabel.setForeground(Color.white);
        messageLabel.setFont(messageLabel.getFont().deriveFont(16f));
        messageLabel.setText(content);

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(messageLabel, BorderLayout.CENTER);

        return contentPanel;
    }

    /**
     * Panel contains the external links
     */
    private JPanel createOptionLinkPanel() {
        JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // resubmission dialog
        resubmissionDialog = new ResubmissionDialog(app.getMainFrame());
        resubmissionDialog.setLocationRelativeTo(app.getMainFrame());
        resubmissionDialog.setVisible(false);

        Icon icon = GUIUtilities.loadIcon(appContext.getProperty("resubmission.button.small.icon"));
        JButton resubmissionButton = GUIUtilities.createLabelLikeButton(icon, appContext.getProperty("resubmission.button.label"));
        resubmissionButton.setForeground(ColourUtil.HYPERLINK_COLOUR);
        resubmissionButton.addActionListener(e -> {
            resubmissionDialog.updateState();
            resubmissionDialog.setVisible(true);
        });

        linkPanel.add(resubmissionButton);

        // bulk submission
        JButton loadSubmissionFileButton = GUIUtilities.createLabelLikeButton(new LoadSubmissionFileAction());
        loadSubmissionFileButton.setForeground(ColourUtil.HYPERLINK_COLOUR);
        linkPanel.add(loadSubmissionFileButton);

        // submission guideline
        icon = GUIUtilities.loadIcon(appContext.getProperty("submission.type.submission.tool.link.small.icon"));
        JButton dataButton = GUIUtilities.createLabelLikeButton(icon, appContext.getProperty("submission.type.submission.tool.submission.guideline"));
        dataButton.setForeground(ColourUtil.HYPERLINK_COLOUR);
        dataButton.addActionListener(actionEvent -> HttpUtil.openURL(appContext.getProperty("px.submission.guideline.web.url")));
        linkPanel.add(dataButton);

        // more about proteomexchagne
        JButton moreButton = GUIUtilities.createLabelLikeButton(icon, appContext.getProperty("submission.type.submission.tool.more.detail"));
        moreButton.setForeground(ColourUtil.HYPERLINK_COLOUR);
        moreButton.addActionListener(actionEvent -> HttpUtil.openURL(appContext.getProperty("px.web.url")));
        linkPanel.add(moreButton);

        return linkPanel;
    }

    /**
     * Listener to perform actions when a submission option is selected
     */
    private class SubmissionOptionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(!appContext.isResubmission()){
                if (e.getActionCommand().equals(FULL_SUBMISSION_OPTION)) {
                    appContext.setSubmissionsType(SubmissionTypeConstants.COMPLETE);
                } else if (e.getActionCommand().equals(PARTIAL_SUBMISSION_OPTION)) {
                    appContext.setSubmissionsType(SubmissionTypeConstants.PARTIAL);
                } else if (e.getActionCommand().equals(AFFINITY_SUBMISSION_OPTION)) {
                    appContext.setSubmissionsType(SubmissionTypeConstants.AFFINITY);
                }
            }else{ // If it is a resubmission
                if (e.getActionCommand().equals(FULL_SUBMISSION_OPTION)) {
                    if(!appContext.getSubmissionType().equals(SubmissionTypeConstants.COMPLETE)){
                        resubmissionChangeTypeError = "You have selected a RESUBMISSION COMPLETE project and " +
                                "you cannot change it a PARTIAL submission now unless you deselect RESUBMISSION";
                    }else{
                        resubmissionChangeTypeError = "";
                    }
                } else if (e.getActionCommand().equals(PARTIAL_SUBMISSION_OPTION)) {
                    if(!appContext.getSubmissionType().equals(SubmissionTypeConstants.PARTIAL)){
                        resubmissionChangeTypeError = "You have selected a RESUBMISSION PARTIAL project and " +
                                "you cannot change it to a COMPLETE submission now unless you deselect RESUBMISSION";
                    }else{
                        resubmissionChangeTypeError= "";
                    }
                }
            }
        }
    }

    public PropertyChangeHelper getPropertyChangeHelper() {
        return propertyChangeBroadcaster;
    }

    private PropertyChangeBroadcaster getPropertyChangeBroadcaster() {
        return propertyChangeBroadcaster;
    }

    public class PropertyChangeBroadcaster extends uk.ac.ebi.pride.toolsuite.gui.prop.PropertyChangeHelper {
        // Events
        // Training mode toggle
        public static final String TRAINING_MODE_TOGGLE = "training_mode_toggle";

        public void fireTrainingModeToggle(boolean oldValue, boolean newValue) {
            firePropertyChange(TRAINING_MODE_TOGGLE, oldValue, newValue);
        }
    }

    private class TrainingModeCheckBoxController {
        public void update(JCheckBox checkBox) {
            // Hide the checkbox
            //checkBox.setVisible(appContext.isTrainingModeFlag());
        }

        public void init(JCheckBox trainingModeCheckBox) {
            appContext.setTrainingModeFlag(false);
            update(trainingModeCheckBox);
            //trainingModeCheckBox.doClick();
        }
    }

    /**
     * This listener updates "training mode" status
     */
    private class TrainingModeOptionListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                logger.info("TRAINING MODE ACTIVATED");
                appContext.setTrainingModeFlag(true);
                getPropertyChangeBroadcaster().fireTrainingModeToggle(false, true);
            } else {
                logger.info("TRAINING MODE DE-ACTIVATED");
                appContext.setTrainingModeFlag(false);
                getPropertyChangeBroadcaster().fireTrainingModeToggle(true, false);
            }
            trainingModeCheckBoxController.update((JCheckBox)e.getSource());
        }
    }

    /**
     * This listener updates Controlled Access Data Mode selection
     */
    private class ControlledAccessDataModeOptionListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                logger.info("CONTROLLED ACCESS MODE ACTIVATED");
                appContext.setControlledAccessModeStatusFlag(true);
            } else {
                logger.info("CONTROLLED ACCESS DE-ACTIVATED");
                appContext.setControlledAccessModeStatusFlag(false);
            }
        }
    }

    /**
     * This method returns the error occured while changeing the Submission type when user trying to a resubmission
     * @return error message
     */
    public String getResubmissionChangeTypeError() {
        return resubmissionChangeTypeError;
    }
}
