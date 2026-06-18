package com.unitynews.contract;

import com.unitynews.contract.FilterSpecDto;

interface IFilterSpecsCallback {
    void onSuccess(in List<FilterSpecDto> specs);
    void onError(String code, String message);
}
