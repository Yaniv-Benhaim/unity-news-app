package com.unitynews.contract;

import com.unitynews.contract.ArticleDto;

interface IArticlesCallback {
    void onSuccess(in List<ArticleDto> articles);
    void onError(String code, String message);
}
