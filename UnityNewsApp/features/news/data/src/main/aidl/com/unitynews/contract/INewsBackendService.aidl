package com.unitynews.contract;

import com.unitynews.contract.ArticleFilterRequest;
import com.unitynews.contract.IArticlesCallback;
import com.unitynews.contract.IFilterSpecsCallback;
import com.unitynews.contract.IBackendStatusCallback;

interface INewsBackendService {
    int getApiVersion();
    void getFilterSpecs(in IFilterSpecsCallback callback);
    void getArticles(in ArticleFilterRequest request, in IArticlesCallback callback);
    void getBackendStatus(in IBackendStatusCallback callback);
}
