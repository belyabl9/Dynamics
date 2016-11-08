<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:page pageTitle="User ${data.user.name}">
    <jsp:attribute name="navBar">
        <t:navBar></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
	    <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="/js/Users.js"></script>
    </jsp:attribute>
	<jsp:body>
        <div id="statusMessage"></div>
		<form id="userDetailsForm">
			<input type="hidden" name="id" value="${data.user.id}" />
			<div class="panel panel-primary pull-left" style="width:45%">
			   <div class="panel-heading">
			       <h3 class="panel-title">
			           User Details
			       </h3>
			   </div>
			   <div class="panel-body">
			   	   <t:userDetails user="${data.user}" />
				   <a class="btn btn-primary pull-right" onclick="Users.save();">Save</a>
			   </div>
			</div>
		</form>

        <div class="panel panel-primary pull-right" style="width:35%">
            <div class="panel-heading">
                <h3 class="panel-title">
                    Statistics
                </h3>
            </div>
            <div class="panel-body">
                <div style="margin-bottom:10px;">
                    <a href="#" class="btn btn-primary" style="margin-left:10px;"
                       onclick="Users.delFeatures(${data.user.id}); return false;">
                       Clear features
                    </a>
                    <a href="/page/userSessions?userId=${data.user.id}" class="btn btn-primary" style="margin-left:10px;">
                       Go to sessions
                    </a>
                </div>
                <table class="table table-bordered table-hover">
                    <tr>
                        <td>
                            Mean Keypress Time, ms
                        </td>
                        <td>
                            <fmt:formatNumber type="number" maxFractionDigits="2" value="${data.user.meanKeypressTime}" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Mean Time Between Keypresses, ms
                        </td>
                        <td>
                            <fmt:formatNumber type="number" maxFractionDigits="2" value="${data.user.meanTimeBetweenKeys}" />
                        </td>
                    </tr>
                </table>
            </div>
        </div>
		<div style="clear:both;"></div>

        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">
                    Learning rate
                </h3>
            </div>
            <div class="panel-body">
                <div class="pull-left legend-keyboard">
                    <h4>Legend</h4>
                    <br>
                    Features
                    <hr>
                    <div style="margin-bottom: 10px;">
                        <div style="float:left;width:12px;height:12px;background-color:white;border:1px solid black;">
                        </div>
                        <span style="margin-left: 10px;">0</span>
                    </div>
                    <div style="margin-bottom: 10px;">
                        <div style="float:left;width:12px;height:12px;background-color:yellow;border:1px solid black;">
                            
                        </div>
                        <span style="margin-left: 10px;">10-30</span>
                    </div>

                    <div style="margin-bottom: 10px;">
                        <div style="float:left;width:12px;height:12px;background-color:green;border:1px solid black;">
                            
                        </div>
                        <span style="margin-left: 10px;">30+</span>
                    </div>
                </div>
                <div id="container">
                    <ul id="keyboard">
                        <li class="symbol"><span class="off">`</span><span class="on">~</span></li>
                        <li class="symbol"><span class="off">1</span><span class="on">!</span></li>
                        <li class="symbol"><span class="off">2</span><span class="on">@</span></li>
                        <li class="symbol"><span class="off">3</span><span class="on">#</span></li>
                        <li class="symbol"><span class="off">4</span><span class="on">$</span></li>
                        <li class="symbol"><span class="off">5</span><span class="on">%</span></li>
                        <li class="symbol"><span class="off">6</span><span class="on">^</span></li>
                        <li class="symbol"><span class="off">7</span><span class="on">&amp;</span></li>
                        <li class="symbol"><span class="off">8</span><span class="on">*</span></li>
                        <li class="symbol"><span class="off">9</span><span class="on">(</span></li>
                        <li class="symbol"><span class="off">0</span><span class="on">)</span></li>
                        <li class="symbol"><span class="off">-</span><span class="on">_</span></li>
                        <li class="symbol"><span class="off">=</span><span class="on">+</span></li>
                        <li class="delete lastitem">delete</li>
                        <li class="tab">tab</li>
                        <li class="letter">q</li>
                        <li class="letter">w</li>
                        <li class="letter">e</li>
                        <li class="letter">r</li>
                        <li class="letter">t</li>
                        <li class="letter">y</li>
                        <li class="letter">u</li>
                        <li class="letter">i</li>
                        <li class="letter">o</li>
                        <li class="letter">p</li>
                        <li class="symbol"><span class="off">[</span><span class="on">{</span></li>
                        <li class="symbol"><span class="off">]</span><span class="on">}</span></li>
                        <li class="symbol lastitem"><span class="off">\</span><span class="on">|</span></li>
                        <li class="capslock">caps lock</li>
                        <li class="letter">a</li>
                        <li class="letter">s</li>
                        <li class="letter">d</li>
                        <li class="letter">f</li>
                        <li class="letter">g</li>
                        <li class="letter">h</li>
                        <li class="letter">j</li>
                        <li class="letter">k</li>
                        <li class="letter">l</li>
                        <li class="symbol"><span class="off">;</span><span class="on">:</span></li>
                        <li class="symbol"><span class="off">'</span><span class="on">&quot;</span></li>
                        <li class="return lastitem">return</li>
                        <li class="left-shift">shift</li>
                        <li class="letter">z</li>
                        <li class="letter">x</li>
                        <li class="letter">c</li>
                        <li class="letter">v</li>
                        <li class="letter">b</li>
                        <li class="letter">n</li>
                        <li class="letter">m</li>
                        <li class="symbol"><span class="off">,</span><span class="on">&lt;</span></li>
                        <li class="symbol"><span class="off">.</span><span class="on">&gt;</span></li>
                        <li class="symbol"><span class="off">/</span><span class="on">?</span></li>
                        <li class="right-shift lastitem">shift</li>
                        <li class="space lastitem">&nbsp;</li>
                    </ul>
                </div>
            </div>
        </div>

	</jsp:body>

</t:page>
