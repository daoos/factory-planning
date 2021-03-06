package com.adarrivi.factory.view.info;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.adarrivi.factory.annealing.PlanningSolution;
import com.adarrivi.factory.auditor.satisfaction.SatisfactionAuditor;
import com.adarrivi.factory.auditor.satisfaction.ScoreDetails;

public class ScoreInfoPanel extends JPanel implements Observer {
    private static final long serialVersionUID = 1L;

    private JLabel processLabel;
    private JTextArea scoreTextArea;

    public ScoreInfoPanel() {
        setLayout(new BorderLayout(0, 0));
        createProcessPanel();
        createScorePanel();
    }

    private void createProcessPanel() {
        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);
        processLabel = new JLabel("...");
        panel.add(processLabel);
    }

    private void createScorePanel() {
        scoreTextArea = new JTextArea();
        scoreTextArea.setEditable(false);
        add(scoreTextArea, BorderLayout.CENTER);
    }

    @Override
    public void update(Observable o, Object currentSolution) {
        PlanningSolution solution = (PlanningSolution) currentSolution;
        processLabel.setText("Best solution with score " + solution.getScore() + ", at temperature: " + solution.getTemperature()
                + " (plannings created: " + solution.getPlanningsCreatedSoFar() + ")");
        SatisfactionAuditor auditor = new SatisfactionAuditor(solution.getPlanning());
        auditor.auditPlanning();
        List<ScoreDetails> allRuleScores = auditor.getAllRuleScores();
        List<String> scoreInfos = allRuleScores.stream().map(this::getScoreInfo).collect(Collectors.toList());
        scoreTextArea.setText(String.join("\n", scoreInfos));
        repaint();
    }

    private String getScoreInfo(ScoreDetails scoreDetails) {
        return scoreDetails.getRuleName() + ": " + scoreDetails.getScorePerOccurence() + " x " + scoreDetails.getOccurences() + " = "
                + scoreDetails.getScore();
    }

}
