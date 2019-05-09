$(function() {
    loadData()
});

function updateViewGames(data) {
  var htmlList = data.games.map(function (games) {
      return  '<li class="list-group-item">' + new Date(games.crationDate).toLocaleString() + ' ' + games.gamePlayers.map(function(p) { return p.player.email}).join(', ')  +'</li>';
  }).join('');
  document.getElementById("game-list").innerHTML = htmlList;
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