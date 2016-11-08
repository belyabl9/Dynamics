package com.m1namoto.page;

public class EvalClassifierPageData extends PageData {
    private String evalResults;
    private String configuration;

    public EvalClassifierPageData(String results, String configuration) {
        this.evalResults = results;
        this.configuration = configuration;
    }
    
    public String getEvalResults() {
        return evalResults;
    }

    public void setEvalResults(String evalResults) {
        this.evalResults = evalResults;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
    
}
