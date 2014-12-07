package com.adarrivi.factory.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.encog.ml.genetic.genome.CalculateGenomeScore;
import org.encog.ml.genetic.genome.Genome;

import com.adarrivi.factory.auditor.satisfaction.SatisfactionAuditor;

public class Planning implements CalculateGenomeScore {

    private List<String> allLines = new ArrayList<>();
    private List<Worker> allWorkers = new ArrayList<>();
    private List<Integer> allDays = new ArrayList<>();

    public Planning(List<String> allLines, List<Worker> allWorkers, List<Integer> allDays) {
        this.allLines = allLines;
        this.allWorkers = allWorkers;
        this.allDays = allDays;
    }

    public int getPlanningSize() {
        return allDays.size() * allDays.size();
    }

    public List<String> getAllLines() {
        return allLines;
    }

    @Override
    public double calculateScore(Genome genome) {
        SatisfactionAuditor auditor = new SatisfactionAuditor((Planning) genome.getOrganism());
        auditor.auditPlanning();
        return auditor.getScore();
    }

    @Override
    public boolean shouldMinimize() {
        return true;
    }

    public List<Worker> getAllWorkers() {
        return allWorkers;
    }

    public List<Integer> getAllDays() {
        return allDays;
    }

    public void setNewShift(String workerName, int day, String line, ShiftType shiftType) {
        Worker worker = getWorkerByName(workerName);
        worker.setShift(day, line, shiftType);
    }

    private Worker getWorkerByName(String workerName) {
        return allWorkers.stream().filter(worker -> worker.getName().equals(workerName)).findAny().get();
    }

    public List<WorkerDay> getAllWorkingShifts(int day) {
        return getAllWorkingShiftsSameDay(day).filter(WorkerDay::isWorkingDay).collect(Collectors.toList());
    }

    private Stream<WorkerDay> getAllWorkingShiftsSameDay(int day) {
        return getAllWorkers().stream().map(aWorker -> aWorker.getDay(day));
    }

    List<WorkerDay> getAllWorkingShiftsRequired(int day) {
        return allLines.stream().flatMap(line -> WorkerDay.createAllShiftsForDay(day, line).stream()).collect(Collectors.toList());
    }

    List<WorkerDay> getMissingShifts(int day) {
        List<WorkerDay> allWorkingShiftsRequired = getAllWorkingShiftsRequired(day);
        List<WorkerDay> allWorkingShifts = getAllWorkingShifts(day);
        allWorkingShiftsRequired.removeAll(allWorkingShifts);
        return allWorkingShiftsRequired;
    }

    public Planning duplicate() {
        List<Worker> duplicatedWorkers = allWorkers.stream().map(Worker::duplicate).collect(Collectors.toList());
        return new Planning(allLines, duplicatedWorkers, allDays);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Worker worker : allWorkers) {
            sb.append(worker).append("\n");
        }
        return sb.toString();
    }

}
