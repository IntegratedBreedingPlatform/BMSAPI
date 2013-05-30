package org.generationcp.ibpworkbench.model;

import java.math.BigDecimal;

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

public class TraitsAndMeans {
    private String environments;
    private String genotypes;
    private String mat50Means;
    private String mat50UnitErrors;
    private String podwtMeans;
    private String podwtUnitErrors;
    private String seedwtMeans;
    private String seedwtUnitErrors;

    public String getEnvironments() {
        return environments;
    }

    public void setEnvironments(String environments) {
        this.environments = environments;
    }

    public String getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(String genotypes) {
        this.genotypes = genotypes;
    }

    public String getMat50Means() {
        return mat50Means;
    }

    public void setMat50Means(String mat50Means) {
        this.mat50Means = mat50Means;
    }

    public String getMat50UnitErrors() {
        return mat50UnitErrors;
    }

    public void setMat50UnitErrors(String mat50UnitErrors) {
        this.mat50UnitErrors = mat50UnitErrors;
    }

    public String getPodwtMeans() {
        return podwtMeans;
    }

    public void setPodwtMeans(String podwtMeans) {
        this.podwtMeans = podwtMeans;
    }

    public String getPodwtUnitErrors() {
        return podwtUnitErrors;
    }

    public void setPodwtUnitErrors(String podwtUnitErrors) {
        this.podwtUnitErrors = podwtUnitErrors;
    }

    public String getSeedwtMeans() {
        return seedwtMeans;
    }

    public void setSeedwtMeans(String seedwtMeans) {
        this.seedwtMeans = seedwtMeans;
    }

    public String getSeedwtUnitErrors() {
        return seedwtUnitErrors;
    }

    public void setSeedwtUnitErrors(String seedwtUnitErrors) {
        this.seedwtUnitErrors = seedwtUnitErrors;
    }

    public String toString() {
        return "TraitsAndMeans[environments = " + environments + ", genotypes = " + genotypes +
                ", mat50Means = " + mat50Means + ", mat50UnitErrors = " + mat50UnitErrors +
                ", podwtMeans = " + podwtMeans + ", podwtUnitErrors = " + podwtUnitErrors +
                ", seedwtMeans = " + seedwtMeans + ", seedwtUnitErrors = " + seedwtUnitErrors + "]";
    }
}

