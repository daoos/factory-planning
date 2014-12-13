package com.adarrivi.factory.annealing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adarrivi.factory.auditor.SatisfactionAuditor;
import com.adarrivi.factory.planning.Planning;
import com.adarrivi.factory.problem.PlanningProblemProperties;

public class AnnealingPlanningSolver {

    private static Logger LOGGER = LoggerFactory.getLogger(AnnealingPlanningSolver.class);
    private static final int MAX_ITERATION_BEFORE_GIVING_UP = 10000;

    private PlanningProblemProperties problemProperties;
    private double currentScore = Double.MAX_VALUE;
    private double bestScore = Double.MAX_VALUE;
    private Planning bestPlanning;
    private Planning currentPlanning;
    private int iterationsAtSameTemperature = 0;
    private AnnealingTemperature temperature;

    public AnnealingPlanningSolver(PlanningProblemProperties problemProperties) {
        this.problemProperties = problemProperties;
        this.temperature = new AnnealingTemperature(problemProperties.getPlanning().getAllDays().size());
    }

    public Planning solve() {
        while (!finalizeCriteriaMet()) {
            calculateCurrentIterationScore();
            stepProcess();
        }
        LOGGER.debug("Simulated annealing stoped at temperature {}, iteration {}, bestScore {}", temperature.getRandomDays(),
                iterationsAtSameTemperature, bestScore);
        return bestPlanning;
    }

    private boolean finalizeCriteriaMet() {
        return currentScore < problemProperties.getAcceptableScore() || iterationsAtSameTemperature > MAX_ITERATION_BEFORE_GIVING_UP
                || !temperature.isThereEnergyLeft();
    }

    private void calculateCurrentIterationScore() {
        currentPlanning = problemProperties.getPlanning().duplicate();
        SensiblePlanningRandomizer randomizer = new SensiblePlanningRandomizer(currentPlanning, temperature);
        try {
            currentPlanning = randomizer.randomizePlanning();
            SatisfactionAuditor auditor = new SatisfactionAuditor(currentPlanning);
            auditor.auditPlanning();
            currentScore = auditor.getTotalScore();
        } catch (ImpossibleToSolveException ex) {
            // LOGGER.debug("No solution found for temperature {}, iteration {}",
            // temperature.getRandomDays(), iterationsAtSameTemperature);
            currentScore = Double.MAX_VALUE;
        }
    }

    private void stepProcess() {
        if (bestScore > currentScore) {
            bestScore = currentScore;
            bestPlanning = currentPlanning;
            temperature.decreaseTemperature();
            iterationsAtSameTemperature = 0;
            LOGGER.debug("Better score found at temperature {}, iteration {}: {}", temperature.getRandomDays(),
                    iterationsAtSameTemperature, currentScore);
        } else {
            iterationsAtSameTemperature++;
        }
    }

}
