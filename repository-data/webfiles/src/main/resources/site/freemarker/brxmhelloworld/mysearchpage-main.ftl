<#include "../include/imports.ftl">

<@hst.setBundle basename="essentials.homepage"/>
<form name="my-search-form" class="navbar-form" action="assessmentSearch" method="get">
  <#if !hstRequest.requestContext.channelManagerPreviewRequest>
    <div class="input-group">
        <input type="text" class="form-control" placeholder="Enter search text here..." name="query" required="required">
        <div class="input-group-btn">
            <button class="btn btn-default" type="submit"><i class="glyphicon glyphicon-search"></i></button>
        </div>
    </div>
  </#if>
</form>