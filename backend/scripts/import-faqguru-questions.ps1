param(
    [string]$ApiBaseUrl = 'http://127.0.0.1:8080/api',
    [string]$AdminUsername = 'admin_console',
    [string]$AdminPassword = $env:AI_INTERVIEW_ADMIN_PASSWORD,
    [int]$MaxCount = 360,
    [string[]]$Topics = @(
        'java',
        'javascript',
        'sql',
        'nodejs',
        'react',
        'vuejs',
        'data-structures',
        'design-patterns',
        'git',
        'mongodb',
        'aws',
        'html5',
        'css',
        'typeScript',
        'graphql',
        'code-problems'
    )
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    throw 'AdminPassword is required. Pass -AdminPassword or set AI_INTERVIEW_ADMIN_PASSWORD.'
}

function Normalize-QuestionTitle {
    param([string]$Title)

    $normalized = $Title.ToLowerInvariant()
    $normalized = [regex]::Replace($normalized, '[^a-z0-9]+', ' ')
    return $normalized.Trim()
}

function Get-TopicDisplayName {
    param([string]$TopicKey)

    $labels = @{
        'java' = 'Java'
        'javascript' = 'JavaScript'
        'sql' = 'SQL'
        'nodejs' = 'Node.js'
        'react' = 'React'
        'vuejs' = 'Vue.js'
        'data-structures' = 'Data Structures'
        'design-patterns' = 'Design Patterns'
        'git' = 'Git'
        'mongodb' = 'MongoDB'
        'aws' = 'AWS'
        'html5' = 'HTML5'
        'css' = 'CSS'
        'typeScript' = 'TypeScript'
        'graphql' = 'GraphQL'
        'code-problems' = 'Code Problems'
    }

    if ($labels.ContainsKey($TopicKey)) {
        return $labels[$TopicKey]
    }

    return ((($TopicKey -replace '-', ' ') -split ' ') | ForEach-Object {
        if ([string]::IsNullOrWhiteSpace($_)) { return $_ }
        $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1).ToLowerInvariant()
    }) -join ' '
}

function Get-QuestionType {
    param(
        [string]$TopicKey,
        [string]$Title
    )

    $algorithmTopics = @('code-problems', 'data-structures')
    if ($algorithmTopics -contains $TopicKey) {
        return 'ALGORITHM'
    }

    $lower = $Title.ToLowerInvariant()
    if ($lower -match 'big-o|time complexity|space complexity|linked list|binary tree|graph|queue|stack|heap|hash table|sorting|sort|search|recursion|dynamic programming|interval|permutation|trie|b-tree|red-black') {
        return 'ALGORITHM'
    }

    return 'INTERVIEW'
}

function Get-QuestionDifficulty {
    param(
        [string]$Title,
        [string]$Type
    )

    $lower = $Title.ToLowerInvariant()

    if ($Type -eq 'ALGORITHM') {
        if ($lower -match 'dynamic programming|graph|interval|permutation|balanced bracket|cube root|red-black|b-tree|deadlock|monitor|thread synchronization|marshalling') {
            return 'HARD'
        }
        if ($lower -match 'missing value|bubble sort|queue using two stacks|linked list|hashmap|iterator|big-o|stack|queue|tree|sort|search|recursion|array') {
            return 'MEDIUM'
        }
        return 'MEDIUM'
    }

    if ($lower -match '^(what is|what are|define|name|list)\b') {
        return 'EASY'
    }
    if ($lower -match 'architecture|life cycle|best practices|tradeoff|deadlock|security|distributed|optimi|performance|internals|concurrency|synchronization|how does') {
        return 'HARD'
    }
    return 'MEDIUM'
}

function Build-QuestionContent {
    param(
        [string]$TopicKey,
        [string]$Title,
        [string]$Type
    )

    $topicName = Get-TopicDisplayName -TopicKey $TopicKey

    if ($Type -eq 'ALGORITHM') {
        return @"
Track: $topicName
Source: FAQGURU (MIT)
Prompt: $Title

Answer as if this is a live coding interview. Explain the approach, core data structures, time complexity, space complexity, and provide key pseudocode or code.
"@.Trim()
    }

    return @"
Track: $topicName
Source: FAQGURU (MIT)
Prompt: $Title

Answer as if this is a real technical interview. Cover the core concept, suitable scenarios, tradeoffs, implementation details, and common follow-up questions.
"@.Trim()
}

function Get-GitHubJson {
    param([string]$Uri)

    Invoke-RestMethod -Headers @{ 'User-Agent' = 'Codex' } -Uri $Uri
}

function Get-MarkdownText {
    param([string]$Uri)

    Invoke-RestMethod -Headers @{ 'User-Agent' = 'Codex' } -Uri $Uri
}

function Get-TopicFiles {
    $files = Get-GitHubJson -Uri 'https://api.github.com/repos/FAQGURU/FAQGURU/contents/topics/en'
    $fileMap = @{}
    foreach ($file in $files) {
        if ($file.type -ne 'file' -or -not $file.name.EndsWith('.md')) {
            continue
        }

        $topicKey = [System.IO.Path]::GetFileNameWithoutExtension($file.name)
        $fileMap[$topicKey] = $file.download_url
    }

    $result = [System.Collections.Generic.List[object]]::new()
    foreach ($topic in $Topics) {
        if (-not $fileMap.ContainsKey($topic)) {
            continue
        }

        $result.Add([pscustomobject]@{
            topicKey = $topic
            downloadUrl = $fileMap[$topic]
        })
    }

    if ($result.Count -eq 0) {
        throw 'No topic files matched the provided Topics list.'
    }

    return $result
}

function Get-CandidateQuestions {
    $seen = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $perTopic = @{}

    foreach ($file in (Get-TopicFiles)) {
        $markdown = Get-MarkdownText -Uri $file.downloadUrl
        $matches = [regex]::Matches($markdown, '(?m)^###\s+(.+?)\s*$')
        $topicCandidates = [System.Collections.Generic.List[object]]::new()

        foreach ($match in $matches) {
            $title = $match.Groups[1].Value.Trim()
            if ([string]::IsNullOrWhiteSpace($title)) {
                continue
            }

            $normalized = Normalize-QuestionTitle -Title $title
            if ([string]::IsNullOrWhiteSpace($normalized) -or $seen.Contains($normalized)) {
                continue
            }

            [void]$seen.Add($normalized)
            $type = Get-QuestionType -TopicKey $file.topicKey -Title $title
            $difficulty = Get-QuestionDifficulty -Title $title -Type $type
            $content = Build-QuestionContent -TopicKey $file.topicKey -Title $title -Type $type

            $topicCandidates.Add([pscustomobject]@{
                title = $title
                content = $content
                type = $type
                difficulty = $difficulty
                topicKey = $file.topicKey
                normalizedTitle = $normalized
            })
        }

        $perTopic[$file.topicKey] = $topicCandidates
    }

    $candidates = [System.Collections.Generic.List[object]]::new()
    while ($candidates.Count -lt $MaxCount) {
        $addedInRound = $false

        foreach ($topic in $Topics) {
            if (-not $perTopic.ContainsKey($topic)) {
                continue
            }

            $queue = $perTopic[$topic]
            if ($queue.Count -eq 0) {
                continue
            }

            $candidates.Add($queue[0])
            $queue.RemoveAt(0)
            $addedInRound = $true

            if ($candidates.Count -ge $MaxCount) {
                break
            }
        }

        if (-not $addedInRound) {
            break
        }
    }

    return $candidates
}

function Invoke-ApiJson {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers,
        [object]$Body = $null
    )

    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        ContentType = 'application/json; charset=utf-8'
    }

    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 6 -Compress)
    }

    Invoke-RestMethod @params
}

$loginResponse = Invoke-ApiJson -Method 'Post' -Uri ($ApiBaseUrl.TrimEnd('/') + '/auth/login') -Headers @{} -Body @{
    username = $AdminUsername
    password = $AdminPassword
}

$token = $loginResponse.data.token
if ([string]::IsNullOrWhiteSpace($token)) {
    throw 'Login succeeded but no token was returned.'
}

$headers = @{ Authorization = 'Bearer ' + $token }
$existingResponse = Invoke-RestMethod -Method 'Get' -Uri ($ApiBaseUrl.TrimEnd('/') + '/questions') -Headers $headers
$existingTitles = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($question in $existingResponse.data) {
    [void]$existingTitles.Add((Normalize-QuestionTitle -Title $question.title))
}

$candidates = Get-CandidateQuestions
$created = 0
$skipped = 0
$createdByType = @{ ALGORITHM = 0; INTERVIEW = 0 }
$createdByDifficulty = @{ EASY = 0; MEDIUM = 0; HARD = 0 }
$createdByTopic = @{}

foreach ($candidate in $candidates) {
    if ($existingTitles.Contains($candidate.normalizedTitle)) {
        $skipped++
        continue
    }

    [void]$existingTitles.Add($candidate.normalizedTitle)
    $null = Invoke-ApiJson -Method 'Post' -Uri ($ApiBaseUrl.TrimEnd('/') + '/admin/questions') -Headers $headers -Body @{
        title = $candidate.title
        content = $candidate.content
        type = $candidate.type
        difficulty = $candidate.difficulty
    }

    $created++
    $createdByType[$candidate.type]++
    $createdByDifficulty[$candidate.difficulty]++
    if (-not $createdByTopic.ContainsKey($candidate.topicKey)) {
        $createdByTopic[$candidate.topicKey] = 0
    }
    $createdByTopic[$candidate.topicKey]++
}

$summary = [pscustomobject]@{
    requestedMaxCount = $MaxCount
    candidateCount = $candidates.Count
    created = $created
    skippedExisting = $skipped
    createdByType = $createdByType
    createdByDifficulty = $createdByDifficulty
    createdByTopic = $createdByTopic
    source = 'FAQGURU/FAQGURU (topics/en, MIT)'
    topics = $Topics
}

$summary | ConvertTo-Json -Depth 6