$(function() {
  loadData();
  $("#login-btn").click(function(){
    login();
  });
  $("#logout-btn").click(function(){
    logout();
  });
});

function updateViewGames(data) {
  var userTxt = data.player;
  var htmlList = data.games.map(function (games) {
      return  '<li class="list-group-item">' + new Date(games.crationDate).toLocaleString() + ' ' + games.gamePlayers.map(function(p) { return p.player.email}).join(', ')  +'</li>';
  }).join('');
  $("#game-list").html(htmlList);
  if(userTxt!="Guest"){
    $("#user-info").text('Hello ' + userTxt.name + '!');
    showLogin(false);
  }
}

function updateViewLBoard(data) {
  var htmlList = data.map(function (score) {
      return  '<tr><td>' + score.name + '</td>'
              + '<td>' + score.score.total + '</td>'
              + '<td>' + score.score.won + '</td>'
              + '<td>' + score.score.lost + '</td>'
              + '<td>' + score.score.tied + '</td></tr>';
  }).join('');
  document.getElementById("leader-list").innerHTML = htmlList;
}

function loadData() {
  $.get("/api/games")
    .done(function(data) {
      updateViewGames(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
  
  $.get("/api/leaderBoard")
    .done(function(data) {
      updateViewLBoard(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
}

function login(){
  $.post("/api/login", { username: $("#username").val(), password: $("#password").val()})
    .done(function() {
      loadData(),
      showLogin(false);
    });
}

function logout(){
  $.post("/api/logout")
    .done(function() { 
      showLogin(true);
    });
}

function showLogin(show){
  if(show){
    $("#login-panel").show();
    $("#user-panel").hide();
  } else {
    $("#login-panel").hide();
    $("#user-panel").show();
  }
}