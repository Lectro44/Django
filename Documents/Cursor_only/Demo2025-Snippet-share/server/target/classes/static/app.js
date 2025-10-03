(function(){
  'use strict';

  angular.module('SnippetApp', ['ngRoute'])
    .run(['$rootScope', '$location', function($rootScope, $location){
      // Check login state on app start
      $rootScope.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      $rootScope.userName = localStorage.getItem('userName') || '';
      $rootScope.isAdmin = localStorage.getItem('isAdmin') === 'true';
      $rootScope.adminName = localStorage.getItem('adminName') || '';
      
      // Update navbar based on login state
      function updateNavbar() {
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var loginLink = document.getElementById('loginLink');
        var registerLink = document.getElementById('registerLink');
        var guestLink = document.getElementById('guestLink');
        var signOutLink = document.getElementById('signOutLink');
        var homeLink = document.getElementById('homeLink');
        if (isLoggedIn) {
          if (loginLink) loginLink.style.display = 'none';
          if (registerLink) registerLink.style.display = 'none';
          if (guestLink) guestLink.style.display = 'none';
          if (signOutLink) signOutLink.style.display = 'inline-block';
          if (homeLink) homeLink.style.display = 'none';
        } else {
          if (loginLink) loginLink.style.display = 'inline-block';
          if (registerLink) registerLink.style.display = 'inline-block';
          if (guestLink) guestLink.style.display = 'inline-block';
          if (signOutLink) signOutLink.style.display = 'none';
          if (homeLink) homeLink.style.display = 'inline-block';
        }
      }
      
      // Update navbar on route change
      $rootScope.$on('$routeChangeSuccess', function(){
        $rootScope.onAdminRoute = ($location.path() === '/me/admin');
        updateNavbar();
      });
      $rootScope.onAdminRoute = ($location.path() === '/me/admin');
      $rootScope.$on('$routeChangeStart', function(e, next){
        if (next && next.$$route && next.$$route.originalPath === '/me/admin') {
          var isAdmin = localStorage.getItem('isAdmin') === 'true';
          if (!isAdmin) {
            e.preventDefault();
            $location.path('/admin-login');
          }
        }
      });
      updateNavbar();

      // global signout for navbar link
      window.signOut = function(){
        var name = localStorage.getItem('userName') || '';
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userName');
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('adminName');
        alert(name ? (name + ' signed out successfully') : 'Signed out successfully');
        $rootScope.$applyAsync(function(){
          $location.path('/welcome');
        });
      };

      // global admin signout for navbar link
      window.adminSignOut = function(){
        var name = localStorage.getItem('adminName') || '';
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('adminName');
        alert(name ? (name + ' signed out successfully') : 'Admin signed out successfully');
        $rootScope.$applyAsync(function(){
          $location.path('/welcome');
        });
      };

      // On tab close/unload, clean up guest snippets for this session
      window.addEventListener('unload', function(){
        try {
          var guestId = sessionStorage.getItem('guestSessionId');
          if (guestId) {
            // Prefer POST cleanup that accepts guestId in body or query
            var url = '/api/snippets/guest/cleanup?guestId=' + encodeURIComponent(guestId);
            var blob = new Blob([], { type: 'application/json' });
            navigator.sendBeacon(url, blob);
          }
        } catch (e) {}
      });
    }])
    .config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider){
      // Ensure routes work with "#/path" instead of "#!/path"
      $locationProvider.hashPrefix('');
      $routeProvider
        .when('/welcome', { templateUrl: 'tpl/welcome.html', controller: 'WelcomeCtrl', controllerAs: 'vm' })
        .when('/login', { templateUrl: 'tpl/login.html', controller: 'LoginCtrl', controllerAs: 'vm' })
        .when('/register', { templateUrl: 'tpl/register.html', controller: 'RegisterCtrl', controllerAs: 'vm' })
        .when('/app', { templateUrl: 'tpl/app.html', controller: 'MainCtrl', controllerAs: 'vm' })
        .when('/admin-login', { templateUrl: 'tpl/admin-login.html', controller: 'AdminLoginCtrl', controllerAs: 'vm' })
        .when('/me/admin', { templateUrl: 'tpl/admin.html', controller: 'AdminCtrl', controllerAs: 'vm' })
        .otherwise({ redirectTo: '/welcome' });
    }])
    .controller('WelcomeCtrl', ['$timeout', function($timeout){
      var vm = this;
      // Per-tab guest session id
      if (!sessionStorage.getItem('guestSessionId')) {
        sessionStorage.setItem('guestSessionId', (Math.random().toString(36).slice(2,10) + Date.now().toString(36)).slice(0, 12));
      }
      vm.guestSessionId = sessionStorage.getItem('guestSessionId');
      
      // (duplicate carousel init removed)

      vm.codeInput = '';
      vm.fetchedSnippet = null;

      vm.fetchByCode = function() {
        if (!vm.codeInput || vm.codeInput.length !== 6) return;
        $.ajax({
          url: '/api/snippets/code/' + vm.codeInput,
          method: 'GET',
          success: function(data) {
            $timeout(function() {
              vm.fetchedSnippet = data;
            });
          },
          error: function() {
            $timeout(function() {
              vm.fetchedSnippet = null;
              alert('No snippet found for this code');
            });
          }
        });
      };

      vm.copyFetchedSnippet = function(){
        if (!vm.fetchedSnippet || !vm.fetchedSnippet.code) return;
        var text = vm.fetchedSnippet.code;
        if (!navigator.clipboard) {
          var ta = document.createElement('textarea');
          ta.value = text;
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
        } else {
          navigator.clipboard.writeText(text).catch(function(){});
        }
        alert('Snippet copied to clipboard');
      };

      vm.downloadFetchedSnippet = function(){
        if (!vm.fetchedSnippet) return;
        var extMap = { JAVA:'java', JAVASCRIPT:'js', TYPESCRIPT:'ts', PYTHON:'py', GO:'go', CSHARP:'cs', CPP:'cpp', HTML:'html', CSS:'css', SQL:'sql', JSON:'json', YAML:'yml', SHELL:'sh' };
        var ext = extMap[vm.fetchedSnippet.language] || 'txt';
        var filename = (vm.fetchedSnippet.title || 'snippet') + '.' + ext;
        var blob = new Blob([vm.fetchedSnippet.code || ''], { type: 'text/plain;charset=utf-8' });
        var url = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      };

      // Initialize Owl Carousel after view loads
      $timeout(function(){
        if (typeof $ !== 'undefined' && $.fn.owlCarousel) {
          // Destroy existing carousel if it exists
          $('#featureCarousel').trigger('destroy.owl.carousel');
          
          var carousel = $('#featureCarousel').owlCarousel({
            loop: true,
            autoplay: true,
            autoplayTimeout: 7000,
            autoplayHoverPause: true,
            nav: false,
            dots: true,
            center: true,
            margin: 20,
            stagePadding: 50,
            responsive: {
              0: { items: 1, center: true, stagePadding: 20 },
              768: { items: 2, center: false, stagePadding: 30 },
              1024: { items: 3, center: false, stagePadding: 50 }
            }
          });
          // Add click handlers for navigation buttons
          $('#prevBtn').click(function() {
            carousel.trigger('prev.owl.carousel');
          });
          $('#nextBtn').click(function() {
            carousel.trigger('next.owl.carousel');
          });
          // Refresh carousel on window resize
          $(window).on('resize', function() {
            carousel.trigger('refresh.owl.carousel');
          });
        } else {
          console.log('jQuery or Owl Carousel not loaded');
        }
      }, 1000);
    }])
    .controller('RegisterCtrl', ['$http', '$location', function($http, $location){
      var vm = this;
      vm.form = { name: '', password: '' };
      vm.register = function(){
        $http.post('/api/auth/register', vm.form).then(function(){
          alert('Registration successful. Please login.');
          $location.path('/login');
        }, function(err){
          var msg = (err && err.data && err.data.message) || 'Registration failed! Please enter both name and password';
          alert('Registration failed: ' + msg);
        });
      };
    }])
    .controller('LoginCtrl', ['$http', '$location', function($http, $location){
      var vm = this;
      vm.form = { name: '', password: '' };

      vm.login = function() {
        if (!vm.form.name || !vm.form.password) {
          alert('Please enter both name and password');
          return;
        }

        $http.post('/api/auth/login', vm.form).then(function(response) {
          localStorage.setItem('isLoggedIn', 'true');
          var name = (response && response.data && response.data.name) || vm.form.name;
          localStorage.setItem('userName', name);
          alert('Login successful');
          $location.path('/app');
        }, function(err) {
          var msg = (err && err.data && err.data.message) || 'Login failed! Please check your credentials';
          alert('Login failed: ' + msg);
        });
      };
    }])
    .controller('MainCtrl', ['$http', '$sce', '$timeout', function($http, $sce, $timeout){
      var vm = this;
      // Ensure per-tab guest session id exists for this controller too
      if (!sessionStorage.getItem('guestSessionId')) {
        sessionStorage.setItem('guestSessionId', (Math.random().toString(36).slice(2,10) + Date.now().toString(36)).slice(0, 12));
      }
      vm.guestSessionId = sessionStorage.getItem('guestSessionId');
      vm.form = { title: '', code: '', language: 'JAVA' };
      vm.snippetsGuest = [];
      vm.snippetsUser = [];
      vm.snippetsAll = [];
      vm.snippetsShared = [];
      vm.languages = [];
      vm.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      vm.userName = localStorage.getItem('userName') || '';
      vm.isLoggedInNow = function(){ return localStorage.getItem('isLoggedIn') === 'true'; };
      vm.editingSnippet = null;
      vm.showShareMenu = false;
      vm.share = { users: [], selected: [], permissions: { read: true, write: false } };
      vm.shareTarget = null;
      vm.disableLanguageDropdown = false;
      vm.searchQuery = '';
      vm.searchResults = [];
      vm.isSearching = false;
      vm.showSearchBar = false; // Track search bar visibility

      vm.load = function(){
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var userName = localStorage.getItem('userName') || '';

        function updateAll(){
          var combined = (vm.snippetsGuest || []).concat(vm.snippetsUser || []);
          vm.snippetsAll = combined.sort(function(a,b){
            return new Date(b.createdAt) - new Date(a.createdAt);
          });
        }

        $http.get('/api/snippets/guest', { headers: { 'X-Guest-Id': vm.guestSessionId } }).then(function(res){
          vm.snippetsGuest = Array.isArray(res.data) ? res.data : [];
          updateAll();
        });

        if (isLoggedIn && userName) {
          $http.get('/api/snippets/me', { headers: { 'X-User-Name': userName } })
            .then(function(res){
              vm.snippetsUser = Array.isArray(res.data) ? res.data : [];
              updateAll();
            });
          
          // Load shared snippets
          $http.get('/api/snippets/shared/me', { headers: { 'X-User-Name': userName } })
            .then(function(res){
              vm.snippetsShared = Array.isArray(res.data) ? res.data : [];
            });

          // Load users for sharing (exclude self on client side)
          $http.get('/api/auth/users').then(function(res){
            var list = Array.isArray(res.data) ? res.data : [];
            vm.share.users = list.filter(function(u){ return u !== userName; });
          });
        } else {
          vm.snippetsUser = [];
          vm.snippetsShared = [];
          updateAll();
        }

        $http.get('/api/snippets/languages').then(function(res){ vm.languages = res.data; });
      };

      vm.prismAlias = function(lang){
        var map = {
          JAVA: 'java', JAVASCRIPT: 'javascript', TYPESCRIPT: 'typescript', PYTON: 'python', GO: 'go', CSHARP: 'csharp', CPP: 'cpp', HTML: 'markup', CSS: 'css', SQL: 'sql', JSON: 'json', YAML: 'yaml', SHELL: 'bash'
        };
        return map[lang] || 'clike';
      };

      vm.highlight = function(s){
        var alias = vm.prismAlias(s.language);
        var highlighted = Prism.highlight(s.code, Prism.languages[alias] || Prism.languages.clike, alias);
        return $sce.trustAsHtml(highlighted);
      };

      // Count lines in code
      vm.getLineCount = function(code) {
        if (!code) return 0;
        return code.split('\n').length;
      };

      // Check if snippet should show expand button
      vm.shouldShowExpand = function(snippet) {
        return vm.getLineCount(snippet.code) > 7;
      };

      // Generate line numbers for code
      vm.generateLineNumbers = function(code) {
        if (code === null || code === undefined) return '';
        // Normalize to ensure code and numbers align: count visual lines exactly
        var normalized = String(code).replace(/\r\n?/g, '\n');
        // If code ends with a newline, do not add an extra empty line number at the end
        var hasTrailing = /\n$/.test(normalized);
        var lines = normalized.split('\n');
        if (hasTrailing && lines.length > 0 && lines[lines.length - 1] === '') {
          lines.pop();
        }
        var out = '';
        for (var i = 1; i <= lines.length; i++) {
          out += i + (i < lines.length ? '\n' : '');
        }
        return out;
      };

      // Editor gutter line numbers (same logic)
      vm.generateLineNumbersEditor = function(code) {
        return vm.generateLineNumbers(code || '');
      };

      // Autosize textarea and keep gutter height in sync
      vm.onEditorInput = function(){
        setTimeout(function(){
          var ta = document.querySelector('.editor-content textarea');
          if (ta) {
            ta.style.height = 'auto';
            ta.style.height = Math.min(600, Math.max(180, ta.scrollHeight)) + 'px';
          }
        }, 0);
      };

      // Get total line count for admin statistics
      vm.getTotalLineCount = function(snippets) {
        if (!snippets || !Array.isArray(snippets)) return 0;
        return snippets.reduce(function(total, snippet) {
          return total + vm.getLineCount(snippet.code);
        }, 0);
      };

      vm.create = function(){
        // Check for duplicate titles
        if (vm.checkDuplicateTitle(vm.form.title)) {
          alert('A snippet with this title already exists. Please choose a different title.');
          return;
        }

        var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        var headers = {};
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var userName = localStorage.getItem('userName') || '';
        if (isLoggedIn && userName) { headers['X-User-Name'] = userName; } else { headers['X-Guest-Id'] = vm.guestSessionId; }
        $http.post('/api/snippets', payload, { headers: headers }).then(function(){
          alert('Snippet added successfully');
          vm.form.code = '';
          vm.form.title = '';
          vm.load();
        }, function(err){
          console.error('Create failed', err);
          var msg = (err && err.data && err.data.message) || 'Failed to create snippet';
          alert(msg);
        });

      // (composer carousel init removed)
        vm.disableLanguageDropdown = true;
        setTimeout(function(){
          var composer = document.querySelector('.composer');
          if (composer) composer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 100);
      };

      vm.updateSnippet = function(){
        if (!vm.editingSnippet) return;
        
        var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        var headers = {};
        var userName = localStorage.getItem('userName') || '';
        if (vm.isLoggedIn && userName) { headers['X-User-Name'] = userName; } else { headers['X-Guest-Id'] = vm.guestSessionId; }
        
        $http.put('/api/snippets/' + vm.editingSnippet.id, payload, { headers: headers }).then(function(){
          alert('Snippet updated successfully');
          vm.form.code = '';
          vm.form.title = '';
          vm.editingSnippet = null;
          vm.load();
        }, function(err){
          console.error('Update failed', err);
          var msg = (err && err.data && err.data.message) || 'Failed to update snippet';
          alert(msg);
        });
      };

      vm.updateOriginal = function(snippet){
        var targetSnippet = snippet || vm.editingSnippet;
        if (!targetSnippet || !targetSnippet.sharedCanEdit) {
          alert('You do not have permission to update the original snippet');
          return;
        }
        
        var userName = localStorage.getItem('userName') || '';
        if (!userName) {
          alert('Please login');
          return;
        }
        
        $http.put('/api/snippets/' + targetSnippet.id + '/push-to-original', {}, { headers: { 'X-User-Name': userName } }).then(function(){
          alert('Original snippet updated successfully');
          if (vm.editingSnippet) {
            vm.form.code = '';
            vm.form.title = '';
            vm.editingSnippet = null;
          }
          vm.load();
        }, function(err){
          console.error('Update original failed', err);
          var msg = (err && err.data && err.data.message) || 'Failed to update original snippet';
          alert(msg);
        });
      };

      vm.cancelEdit = function(){
        vm.editingSnippet = null;
        vm.form.code = '';
        vm.form.title = '';
        vm.form.language = 'JAVA';
      };

      vm.copyCode = function(snippet){
        if (!navigator.clipboard) {
          var ta = document.createElement('textarea');
          ta.value = snippet.code || '';
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
        } else {
          navigator.clipboard.writeText(snippet.code || '').then(function() {
            alert('Code copied to clipboard');
          }, function(err) {
            console.error('Could not copy text: ', err);
          });
        }
      };

      // Copy helper for share code
      vm.copyText = function(text){
        if (!text) return;
        if (!navigator.clipboard) {
          var ta = document.createElement('textarea');
          ta.value = text;
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
        } else {
          navigator.clipboard.writeText(text).catch(function(){});
        }
        alert('Copied to clipboard');
      };

      vm.downloadCode = function(snippet){
        var extMap = { JAVA:'java', JAVASCRIPT:'js', TYPESCRIPT:'ts', PYTHON:'py', GO:'go', CSHARP:'cs', CPP:'cpp', HTML:'html', CSS:'css', SQL:'sql', JSON:'json', YAML:'yml', SHELL:'sh' };
        var ext = extMap[snippet.language] || 'txt';
        var filename = (snippet.title || 'snippet') + '.' + ext;
        var blob = new Blob([snippet.code || ''], { type: 'text/plain;charset=utf-8' });
        var url = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      };

      // Receiver action: push edits back to sender's original (if allowed)
      vm.pushToOriginal = function(snippet){
        if (!snippet || !snippet.id) return;
        if (!snippet.sharedCanEdit) {
          alert('You do not have permission to push changes');
          return;
        }
        var userName = localStorage.getItem('userName') || '';
        if (!userName) {
          alert('Please login');
          return;
        }
        $http.put('/api/snippets/' + snippet.id + '/push-to-original', {}, { headers: { 'X-User-Name': userName } }).then(function(){
          alert('Original updated successfully');
        }, function(){
          alert('Failed to update original');
        });
      };

      // Edit snippet (populate composer)
      vm.editSnippet = function(snippet){
        if (!snippet) return;
        vm.editingSnippet = { 
          id: snippet.id, 
          sharedCanEdit: snippet.sharedCanEdit || false 
        };
        vm.form.title = snippet.title || '';
        vm.form.code = snippet.code || '';
        vm.form.language = snippet.language || 'JAVA';
        vm.disableLanguageDropdown = false;
        $timeout(function(){
          var composer = document.querySelector('.composer');
          if (composer) composer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 50);
      };

      // Delete snippet
      vm.remove = function(id){
        if (!id) return;
        if (!confirm('Delete this snippet?')) return;
        $http.delete('/api/snippets/' + id).then(function(){
          alert('Snippet deleted');
          vm.load();
        }, function(){
          alert('Failed to delete snippet');
        });
      };

      // Share UI functions
      vm.openShareMenu = function(){
        vm.showShareMenu = true;
        vm.shareTarget = null;
        vm.share.selected = [];
        // Default to read-only - ensure proper initialization
        vm.share.permissions = { read: true, write: false };
      };

      vm.openShareFor = function(snippet){
        vm.showShareMenu = true;
        vm.shareTarget = snippet;
        vm.share.selected = [];
        // Ensure proper initialization
        vm.share.permissions = { read: true, write: false };
        $timeout(function(){
          var composer = document.querySelector('.composer');
          if (composer) composer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 50);
      };

      vm.cancelShare = function(){
        vm.showShareMenu = false;
        vm.shareTarget = null;
        vm.share.selected = [];
      };

      vm.performShare = function(){
        if (!vm.isLoggedIn) {
          alert('Please log in to share with users');
          return;
        }
        var userName = localStorage.getItem('userName') || '';
        var headers = { 'X-User-Name': userName };

        function postShare(id){
          // Use the write permission directly (radio buttons ensure mutual exclusivity)
          var canWrite = !!vm.share.permissions.write;
          console.log('Share permissions:', vm.share.permissions, 'canWrite:', canWrite);
          var body = { userNames: vm.share.selected, canWrite: canWrite };
          $http.post('/api/snippets/' + id + '/share-to', body, { headers: headers }).then(function(){
            alert('Snippet shared successfully');
            vm.cancelShare();
            vm.load();
          }, function(){
            alert('Failed to share snippet');
          });
        }

        if (vm.shareTarget && vm.shareTarget.id) {
          postShare(vm.shareTarget.id);
        } else {
          var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
          $http.post('/api/snippets', payload, { headers: headers }).then(function(res){
            var created = res.data;
            if (created && created.id) {
              postShare(created.id);
            } else {
              alert('Snippet created but share failed to start');
            }
          }, function(){
            alert('Failed to create snippet for sharing');
          });
        }
      };

      vm.generateCode = function(snippet) {
        if (!snippet || !snippet.id) {
          alert('Invalid snippet');
          return;
        }

        $http.post('/api/snippets/' + snippet.id + '/generate-code').then(function(res) {
          snippet.shareCode = res.data.code;
          alert('Share code generated: ' + snippet.shareCode);
        }, function(err) {
          alert('Failed to generate share code');
        });
      };

      // Guest share from composer: create and surface auto-generated code
      vm.generateGuestShareCode = function(){
        if (vm.isLoggedIn) {
          alert('Guest share is available only when not logged in');
          return;
        }
        if (!vm.form.title || !vm.form.code) {
          alert('Please enter title and code first');
          return;
        }
        var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        $http.post('/api/snippets', payload, { headers: { 'X-Guest-Id': vm.guestSessionId } }).then(function(res){
          var code = res && res.data && /^\d{6}$/.test(res.data.shareCode) ? res.data.shareCode : null;
          if (code) {
            vm.lastGuestShareCode = code;
            alert('Share code generated: ' + code + '\nAsk others to open Home and enter the code.');
          } else {
            alert('Snippet created. You can generate a code from the list.');
          }
          vm.form.code = '';
          vm.form.title = '';
          vm.load();
        }, function(){
          alert('Failed to create snippet');
        });
      };

      vm.fetchSnippetByCode = function() {
        if (!vm.codeInput || vm.codeInput.length !== 6) {
          alert('Please enter a valid 6-digit code');
          return;
        }

        $http.get('/api/snippets/code/' + vm.codeInput).then(function(res) {
          vm.fetchedSnippet = res.data;
        }, function(err) {
          alert('No snippet found for this code');
        });
      };

      // Search functionality
      vm.searchSnippets = function() {
        if (!vm.searchQuery || !vm.searchQuery.trim()) {
          vm.isSearching = false;
          vm.load();
          return;
        }

        vm.isSearching = true;
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var userName = localStorage.getItem('userName') || '';
        
        if (isLoggedIn && userName) {
          // Search user's snippets
          $http.get('/api/snippets/me', { headers: { 'X-User-Name': userName } })
            .then(function(res){
              var list = res.data || [];
              var q = vm.searchQuery.toLowerCase();
              var filtered = list.filter(function(s){
                return (s.title && s.title.toLowerCase().includes(q)) || (s.code && s.code.toLowerCase().includes(q));
              });
              vm.searchResults = filtered;
              vm.snippetsUser = filtered;
              vm.snippetsAll = filtered;
            }, function(err){
              console.error('Search failed', err);
              alert('Search failed');
            });
        } else {
          // Search guest snippets
          $http.get('/api/snippets/guest', { headers: { 'X-Guest-Id': vm.guestSessionId } })
            .then(function(res){
              var list = res.data || [];
              var q = vm.searchQuery.toLowerCase();
              var filtered = list.filter(function(s){
                return (s.title && s.title.toLowerCase().includes(q)) || (s.code && s.code.toLowerCase().includes(q));
              });
              vm.searchResults = filtered;
              vm.snippetsGuest = filtered;
              vm.snippetsAll = filtered;
            }, function(err){
              console.error('Search failed', err);
              alert('Search failed');
            });
        }
      };

      // Check for duplicate titles
      vm.checkDuplicateTitle = function(title) {
        if (!title || !title.trim()) return false;
        
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var allSnippets = [];
        
        if (isLoggedIn) {
          allSnippets = (vm.snippetsUser || []).concat(vm.snippetsShared || []);
        } else {
          allSnippets = vm.snippetsGuest || [];
        }
        
        return allSnippets.some(function(snippet) {
          return snippet.title && snippet.title.toLowerCase() === title.toLowerCase();
        });
      };

      // Clear search results
      vm.clearSearch = function() {
        vm.searchQuery = '';
        vm.isSearching = false;
        vm.searchResults = [];
        vm.load();
      };

      // Update search bar visibility based on login state
      vm.updateSearchBarVisibility = function() {
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        vm.showSearchBar = isLoggedIn;
      };

      // Ensure search bar functionality works after login
      vm.initializeSearchBar = function() {
        vm.searchQuery = '';
        vm.searchResults = [];
        vm.isSearching = false;
      };

      // Call initializeSearchBar after login
      vm.login = function() {
        // ...existing login logic...
        vm.initializeSearchBar();
      };

      vm.logout = function() {
        // ...existing logout logic..
        vm.showSearchBar = false;
      };

      // Initialize search bar visibility on page load
      vm.updateSearchBarVisibility();

      vm.load();
    }])
    .controller('AdminCtrl', ['$http', '$sce', '$timeout', function($http, $sce, $timeout){
      var vm = this;
      vm.isAdmin = localStorage.getItem('isAdmin') === 'true';
      vm.adminName = localStorage.getItem('adminName') || '';
      vm.users = [];
      vm.snippets = [];
      vm.searchQuery = '';
      vm.selectedUser = null;
      vm.userSnippets = [];
      vm.stats = {
        totalUsers: 0,
        totalSnippets: 0,
        totalShared: 0,
        totalLines: 0
      };

      // Check if admin is logged in
      if (!vm.isAdmin) {
        alert('Please login as admin to access admin panel');
        window.location.href = '#/admin-login';
        return;
      }

      vm.loadUsers = function(){
        $http.get('/api/admin/users', { headers: { 'X-Admin-Name': vm.adminName } }).then(function(res){
          vm.users = res.data || [];
          vm.stats.totalUsers = vm.users.length;
        }, function(err){
          console.error('Failed to load users', err);
          alert('Failed to load users');
        });
      };

      vm.loadSnippets = function(){
        $http.get('/api/admin/snippets', { headers: { 'X-Admin-Name': vm.adminName } }).then(function(res){
          vm.snippets = res.data || [];
          vm.stats.totalSnippets = vm.snippets.length;
          vm.stats.totalShared = vm.snippets.filter(s => s.isShared).length;
          vm.stats.totalLines = vm.getTotalLineCount(vm.snippets);
        }, function(err){
          console.error('Failed to load snippets', err);
          alert('Failed to load snippets');
        });
      };

      vm.searchSnippets = function(){
        if (!vm.searchQuery.trim()) {
          vm.loadSnippets();
          return;
        }
        
        $http.get('/api/admin/snippets/search?q=' + encodeURIComponent(vm.searchQuery), { 
          headers: { 'X-Admin-Name': vm.adminName } 
        }).then(function(res){
          vm.snippets = res.data || [];
        }, function(err){
          console.error('Search failed', err);
          alert('Search failed');
        });
      };

      vm.viewUserSnippets = function(user){
        vm.selectedUser = user;
        vm.loadingUserSnippets = true;
        var uid = encodeURIComponent(user.id);
        var adminName = localStorage.getItem('adminName') || vm.adminName || '';
        $http.get('/api/admin/users/' + uid + '/snippets', { 
          headers: { 'X-Admin-Name': adminName } 
        }).then(function(res){
          vm.userSnippets = res.data || [];
          vm.loadingUserSnippets = false;
        }, function(err){
          console.error('Failed to load user snippets', err);
          var msg = (err && err.data && (err.data.message || err.data.error)) || (err && err.statusText) || 'Failed to load snippets';
          alert(msg);
          vm.loadingUserSnippets = false;
        });
      };

      vm.deleteUser = function(user){
        if (!confirm('Are you sure you want to delete user "' + user.name + '" and all their snippets?')) {
          return;
        }
        
        $http.delete('/api/admin/users/' + user.id, { 
          headers: { 'X-Admin-Name': vm.adminName } 
        }).then(function(){
          alert('User deleted successfully');
          vm.loadUsers();
          vm.loadSnippets();
        }, function(err){
          console.error('Failed to delete user', err);
          alert('Failed to delete user');
        });
      };

      vm.deleteSnippet = function(snippet){
        if (!confirm('Are you sure you want to delete this snippet?')) {
          return;
        }
        
        $http.delete('/api/admin/snippets/' + snippet.id, { 
          headers: { 'X-Admin-Name': vm.adminName } 
        }).then(function(){
          alert('Snippet deleted successfully');
          vm.loadSnippets();
          if (vm.selectedUser) {
            vm.viewUserSnippets(vm.selectedUser);
          }
        }, function(err){
          console.error('Failed to delete snippet', err);
          alert('Failed to delete snippet');
        });
      };

      vm.prismAlias = function(lang){
        var map = {
          JAVA: 'java', JAVASCRIPT: 'javascript', TYPESCRIPT: 'typescript', PYTHON: 'python', GO: 'go', CSHARP: 'csharp', CPP: 'cpp', HTML: 'markup', CSS: 'css', SQL: 'sql', JSON: 'json', YAML: 'yaml', SHELL: 'bash'
        };
        return map[lang] || 'clike';
      };

      vm.highlight = function(s){
        var alias = vm.prismAlias(s.language);
        var highlighted = Prism.highlight(s.code, Prism.languages[alias] || Prism.languages.clike, alias);
        return $sce.trustAsHtml(highlighted);
      };

      // Get total line count for admin statistics
      vm.getTotalLineCount = function(snippets) {
        if (!snippets || !Array.isArray(snippets)) return 0;
        return snippets.reduce(function(total, snippet) {
          return total + vm.getLineCount(snippet.code);
        }, 0);
      };

      // Count lines in code
      vm.getLineCount = function(code) {
        if (!code) return 0;
        return code.split('\n').length;
      };

      // Generate line numbers for code
      vm.generateLineNumbers = function(code) {
        if (!code) return '';
        var lines = code.split('\n');
        var lineNumbers = '';
        for (var i = 1; i <= lines.length; i++) {
          lineNumbers += i + '\n';
        }
        return lineNumbers;
      };

      vm.loadUsers();
      vm.loadSnippets();
    }])
    .controller('AdminLoginCtrl', ['$http', '$location', function($http, $location){
      var vm = this;
      vm.form = { name: '', password: '' };
      vm.login = function(){
        if (!vm.form.name || !vm.form.password) {
          alert('Enter admin name and password');
          return;
        }
        $http.post('/api/admin/login', vm.form).then(function(res){
          localStorage.setItem('isAdmin', 'true');
          var name = (res && res.data && res.data.name) || vm.form.name;
          localStorage.setItem('adminName', name);
          alert('Admin login successful');
          $location.path('/me/admin');
        }, function(){
          alert('Invalid admin credentials');
        });
      };
    }]);
}());
