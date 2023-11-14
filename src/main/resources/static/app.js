document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('dealCards').addEventListener('click', dealCards);
    document.getElementById('restartGame').addEventListener('click', restartGame);


    // Generate seat placeholders
    const seatsContainer = document.getElementById('seats');
    for (let i = 1; i <= 10; i++) {
        const seatDiv = document.createElement('div');
        seatDiv.id = `seat-${i}`;
        seatDiv.className = 'seat';
        seatDiv.innerHTML = `
            <button onclick="attemptToTakeSeat(${i})">Seat ${i}</button>
            <p id="player-name-seat-${i}"></p> <!-- Placeholder for player name -->
        `;
        seatsContainer.appendChild(seatDiv);
    }
});



function dealCards() {
    fetch('/poker/deal', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => response.json())
        .then(hands => {
            // Loop through all the seats
            for (let i = 1; i <= 10; i++) {
                const seatDiv = document.getElementById(`seat-${i}`);
                const playerNameDisplay = seatDiv.querySelector('#player-name-seat-' + i);

                // Check if the seat is occupied (player name exists)
                if (playerNameDisplay && playerNameDisplay.textContent) {
                    const playerName = playerNameDisplay.textContent;
                    const cards = hands[playerName];

                    // Clear previous content and display new card images
                    playerNameDisplay.innerHTML = ''; // Clear previous cards

                    if (cards && cards.length > 0) {
                        let allCardImages = ''; // Initialize a variable to hold all card image elements

                        cards.forEach(card => {
                            const timestamp = new Date().getTime(); // Cache busting
                            const imageFileName = `${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png?${timestamp}`;
                            const imgElement = `<img src="images/cards/${imageFileName}" alt="${card.rank} of ${card.suit}" class="seat-card-img">`;
                            allCardImages += imgElement; // Append each card image to the variable
                        });

                        playerNameDisplay.innerHTML = allCardImages; // Set the innerHTML once with all card images
                        console.log("After appending:", playerNameDisplay.innerHTML);
                    }
                }
            }
        })
        .catch(error => {
            console.error('Error dealing cards:', error);
        });
}





function restartGame() {
    fetch('/poker/restart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // If you have CSRF protection enabled, you need to add the CSRF token here.
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to restart the game');
            }
            // Clear the hands displayed in the game area without removing player names
            const gameArea = document.getElementById('gameArea');
            gameArea.querySelectorAll('.player-hand').forEach(playerHandDiv => {
                // Assume each player hand div contains an <h3> for the player's name and <img> for cards
                const playerNameHeading = playerHandDiv.querySelector('h3');
                playerHandDiv.innerHTML = ''; // Clear the hand
                playerHandDiv.appendChild(playerNameHeading); // Re-add the player's name heading
            });
        })
        .catch(error => {
            console.error('Error restarting game:', error);
            document.getElementById('gameArea').innerText = 'Error restarting the game.';
        });
}


function attemptToTakeSeat(seatNumber) {
    const playerNameInput = document.getElementById('playerName');
    const playerName = playerNameInput.value.trim();
    if (!playerName) {
        alert('Please enter your name.');
        return;
    }

    takeSeat(playerName, seatNumber);
}

function takeSeat(playerName, seatNumber) {
    fetch('/poker/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        },
        body: JSON.stringify({ playerName, seatNumber })
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text || 'Unable to take seat') });
            }
            return response.json();
        })
        .then(data => {
            const playerNameDisplay = document.getElementById(`player-name-seat-${seatNumber}`);
            playerNameDisplay.textContent = playerName;
            document.querySelector(`#seat-${seatNumber} button`).disabled = true;
        })
        .catch(error => {
            console.error('Error taking seat:', error);
            alert(error.message);
        });
}



// Initialize seat dropdown
const seatSelection = document.getElementById('seatSelection');
for (let i = 1; i <= 10; i++) {
    const option = document.createElement('option');
    option.value = i;
    option.text = `Seat ${i}`;
    seatSelection.appendChild(option);
}

// Handle sit down action
document.getElementById('sitDown').addEventListener('click', () => {
    const playerName = document.getElementById('playerName').value.trim();
    const selectedSeat = seatSelection.value;

    if (!playerName) {
        alert('Please enter your name.');
        return;
    }

    if (!selectedSeat) {
        alert('Please select a seat.');
        return;
    }

    sitDown(playerName, selectedSeat);
});

// Function to manage sitting down logic
function sitDown(playerName, seatNumber) {
    fetch('/poker/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        },
        body: JSON.stringify({ playerName, seatNumber })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // On success, update the UI to show the player's name under the selected seat
                const seatDiv = document.getElementById(`seat-${seatNumber}`);
                const playerNameParagraph = seatDiv.querySelector('p') || document.createElement('p');
                playerNameParagraph.textContent = playerName;
                seatDiv.appendChild(playerNameParagraph);

                // Optionally, disable the seat in the dropdown
                const seatOption = document.querySelector(`#seatSelection option[value="${seatNumber}"]`);
                seatOption.disabled = true;
                seatOption.textContent += ' (taken)';
            } else {
                alert(data.message || 'Unable to take seat');
            }
        })
        .catch(error => {
            console.error('Error taking seat:', error);
        });
}

