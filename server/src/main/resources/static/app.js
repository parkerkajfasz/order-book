const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/trade-feed-websocket'
});

let currentViewDepth = "bbo";

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log("Connected: " + frame);

    stompClient.subscribe("/topic/trades", (message) => {
        const trade = JSON.parse(message.body);

        const priceFormatted = String(trade.priceTraded).padStart(2, ' ');
        showTrade(`[${trade.timestamp}] SMBL @ ${priceFormatted} x ${trade.volumeTraded} ${trade.makerSide.charAt(0)}`);
    });

    stompClient.subscribe("/topic/bbo", (message) => {
        const bbo = JSON.parse(message.body);

        if (bbo.bestBidPrice === 0) bbo.bestBidPrice = "--";
        if (bbo.bestAskPrice === 0) bbo.bestAskPrice = "--";
        if (currentViewDepth === "bbo") showBBO(bbo);
    });

    stompClient.subscribe("/topic/mbp", (message) => {
        const mbp = JSON.parse(message.body);
        if (currentViewDepth === "mbp") showMBP(mbp);
    });

    stompClient.subscribe("/topic/mbo", (message) => {
        const mbo = JSON.parse(message.body);
        if (currentViewDepth === "mbo") showMBO(mbo);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error("Error with websocket", error);
};

stompClient.onStompError = (frame) => {
    console.error("Broker reported error: " + frame.headers["message"]);
    console.error("Additional details: " + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);

    if (connected) {
        $("#trade-table").show();
        showView(currentViewDepth);
    } else {
        $("#trade-table").hide();
        $("#bbo-view").hide();
        $("#mbp-view").hide();
        $("#mbo-view").hide();
    }

    $("#trades").html("");
}

function showView(view) {
    currentViewDepth = view;

    $("#bbo-view").hide();
    $("#mbp-view").hide();
    $("#mbo-view").hide();

    if (view === "bbo") {
        $("#bbo-view").show();
    } else if (view === "mbp") {
        $("#mbp-view").show();
    } else if (view === "mbo") {
        $("#mbo-view").show();
    }
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function showTrade(trade) {
    $("#trades").prepend("<tr style='font-family: monospace; white-space: pre;'><td>" + trade + "</td></tr>");
}

function showBBO(bbo) {
    $("#bbo").html(`
        <tr>
            <td><b>${bbo.bestBidPrice}</b></td>
            <td><b>${bbo.bestAskPrice}</b></td>
        </tr>
    `);
}

function showMBP(mbp) {
    let bidRows = "";
    for (const priceLevel of mbp.bidPriceLevels) {
        bidRows += `
            <tr>
                <td>${priceLevel.orderCount}</td>
                <td>${priceLevel.totalVolume}</td>
                <td>${priceLevel.price}</td>
            </tr>
        `;
    }

    let askRows = "";
    for (const priceLevel of mbp.askPriceLevels) {
        askRows += `
            <tr>
                <td>${priceLevel.price}</td>
                <td>${priceLevel.totalVolume}</td>
                <td>${priceLevel.orderCount}</td>
            </tr>
        `;
    }

    $("#mbp-bids").html(bidRows);
    $("#mbp-asks").html(askRows);
}

function showMBO(mbo) {
    let bidRows = "";
    for (const bid of mbo.bids) {
        bidRows += `
            <tr>
                <td>[${bid.timestamp}]</td>
                <td>${bid.volume}</td>
                <td>${bid.price}</td>
                <td>${bid.side}</td>
            </tr>
        `;
    }

    let askRows = "";
    for (const ask of mbo.asks) {
        askRows += `
            <tr>
                <td>${ask.side}</td>
                <td>${ask.price}</td>
                <td>${ask.volume}</td>
                <td>[${ask.timestamp}]</td>
            </tr>
        `;
    }

    $("#mbo-bids").html(bidRows);
    $("#mbo-asks").html(askRows);
}

$(function () {
    $("form").on("submit", (e) => e.preventDefault());

    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());

    $("#bbo-button").click(() => showView("bbo"));
    $("#mbp-button").click(() => showView("mbp"));
    $("#mbo-button").click(() => showView("mbo"));

    showView("bbo");

    $("#trade-table").hide();
    $("#bbo-view").hide();
    $("#mbp-view").hide();
    $("#mbo-view").hide();
});